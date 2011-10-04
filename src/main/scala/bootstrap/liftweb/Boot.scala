package bootstrap.liftweb

import net.liftweb._
import common._
import http._
import mongodb.{DefaultMongoIdentifier, MongoDB}
import sitemap._
import Loc._
import util.Helpers._
import sitemap.LocPath._
import sshd.SshDaemon
import actors.Actor
import xml.{NodeSeq, Text}
import sshd.git.GitDaemon
import com.mongodb.Mongo
import code.model.UserDoc
case class UserPage(login: String) {
  lazy val user = UserDoc.find("login", login)
}

case class UserRepoPage(userName: String, repoName: String) extends UserPage(userName) {
  lazy val repo = user match {
    case Full(u) => tryo {
      u.repos.filter(_.name.get == repoName).head
    } or {
      Empty
    }
    case _ => Empty
  }
}

case class SourceTreePage(userName: String, repoName: String, path: List[String]) {
  lazy val user = UserDoc.find(UserDoc.login.name, userName)
  lazy val repo = user match {
    case Full(uu) => tryo { uu.repos.filter(_.name.get == repoName).head } or { Empty }
    case _ => Empty
  }
}


// TODO FIXME
object ValidUser {
  def unapply(login: String): Option[String] = Full(login)

  //User.withLogin(login) match {
  //  case Full(u) => Full(login)
  //  case _ => None
  //}
}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  def boot {
    MongoDB.defineDb(DefaultMongoIdentifier, new Mongo, "grt") //TODO както секюрно надо это делать

    new Actor {
      def act() {
        SshDaemon.start
      }
    }.start()

    new Actor {
      def act() {
        GitDaemon.start
      }
    }.start()

    ResourceServer.allow {
      case "css" :: _ => true
      case "js" :: _ => true
    }

    // where to search snippet
    LiftRules.addToPackages("code")

    LiftRules.explicitlyParsedSuffixes = Set()

    val indexPage = Menu.i("Home") / "index" >> If(() => !UserDoc.loggedIn_?, () => RedirectResponse("/list/" + UserDoc.currentUserId.open_!))
    // val listPage = Menu.i("List") / "list"

    val userPage = Menu.param[UserPage]("userPage",
      new LinkText[UserPage](up => Text("User " + up.login)),
      login => Full(UserPage(login)),
      up => up.login) / "list" / * >> Template(() => Templates("list" :: Nil) openOr NodeSeq.Empty)

    val userAdminPage = Menu.param[UserPage]("userAdminPage",
      new LinkText[UserPage](up => Text("User " + up.login)),
      login => Full(UserPage(login)),
      up => up.login) / "admin" / * >> Template(() => Templates("admin" :: "adminUser" :: Nil) openOr NodeSeq.Empty)

    val userRepoAdminPage = Menu.params[UserRepoPage]("userRepoAdminPage",
      new LinkText[UserRepoPage](urp => Text("Repo " + urp.repoName)),
      list => list match {
        case login :: repo :: Nil => Full(UserRepoPage(login, repo))
        case _ => Empty
      },
      urp => urp.login :: urp.repoName :: Nil) / "admin" / * / * >> Template(() => Templates("admin" :: "adminRepo" :: Nil) openOr NodeSeq.Empty)

    val sourceTreePage = Menu.params[SourceTreePage]("sourceTreePage",
      new LinkText[SourceTreePage](stp => Text("Repo " + stp.repoName)),
      list => {

        list match {
          case login :: repo :: path => Full(SourceTreePage(login, repo, path))
          case _ => Empty
        }
      },
      stp => (stp.userName :: stp.repoName :: Nil) ::: stp.path) / * / * / "tree" / ** >> Template(() => Templates("repo" :: "tree" :: Nil) openOr NodeSeq.Empty)
    val blobPage = Menu.params[SourceTreePage]("blobPage",
          new LinkText[SourceTreePage](stp => Text("Repo " + stp.repoName)),
          list => {

            list match {
              case login :: repo :: path => Full(SourceTreePage(login, repo, path))
              case _ => Empty
            }
          },
          stp => (stp.userName :: stp.repoName :: Nil) ::: stp.path) / * / * / "blob" / ** >> Template(() => Templates("repo" :: "blob" :: Nil) openOr NodeSeq.Empty)



    val signInPage = Menu.i("Sign In") / "user" / "signin"

    val loginPage = Menu.i("Log In") / "user" / "login"

    val newUserPage = Menu.i("Registration") / "user" / "new"

    // Build SiteMap
    val entries = List(
      indexPage,
      userPage,
      signInPage,
      loginPage,
      newUserPage,
      userAdminPage,
      userRepoAdminPage,
      sourceTreePage, blobPage)
    //Menu.i("Home") / "index", // the simple way to declare a menu
    //Menu.i("New User") / "new",

    //)


    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath("index" :: Nil, _, _, true), _, _) =>

        RewriteResponse("index" :: Nil, true)
      case RewriteRequest(ParsePath(ValidUser(user) :: Nil, _, _, false), _, _) =>

        RewriteResponse("list" :: user :: Nil, Map[String, String]())
    }


    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMap(SiteMap(entries: _*))

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

  }
}
