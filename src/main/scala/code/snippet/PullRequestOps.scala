/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import bootstrap.liftweb._
import net.liftweb._
import http._
import common._
import util._
import Helpers._
import xml._
import Utility._
import code.model._
import code.snippet.SnippetHelper._

/**
 * User: denis.bardadym
 * Date: 10/24/11
 * Time: 2:43 PM
 */

class PullRequestOps(urp: WithRepo) extends Loggable {

  private var sourceRepo: RepositoryDoc = null

  private var sourceRef = ""

  private var destRepo: RepositoryDoc = null

  private var destRef = ""

  private var description = ""

  def renderUserClones = w(urp.repo){repo => {
    if (!repo.forkOf.valueBox.isEmpty) {
        ".repo_selector" #> SHtml.selectObj(urp.user.get.repos.filter(r => r.forkOf.get == repo.forkOf.get).map(r => r -> r.name.get),
          Full(repo), (r: RepositoryDoc) => {
            sourceRepo = r
          }, "class" -> "selectmenu repo_selector") &
          "name=srcRef" #> SHtml.text(sourceRef, s => {
            sourceRef = s.trim
            if (sourceRef.isEmpty) S.error("Source reference is empty")
          }, "class" -> "textfield")
      }else {
        cleanAll
      }
    }

  }

  def renderAllClones = w(urp.repo){repo => { 
    if (!repo.forkOf.valueBox.isEmpty) {
        ".repo_selector" #> SHtml.selectObj[RepositoryDoc]((repo.forkOf.obj.get -> (repo.forkOf.obj.get.owner.login + "/" + repo.forkOf.obj.get.name.get)) :: RepositoryDoc.allClonesExceptOwner(repo).map(r => r -> (r.owner.login + "/" + r.name.get)),
          repo.forkOf.obj, (r: RepositoryDoc) => {
            destRepo = r
          }, "class" -> "selectmenu repo_selector") &
          "name=destRef" #> SHtml.text(destRef, s => {
            destRef = s.trim
            if (destRef.isEmpty) S.error("Destination reference is empty")
          }, "class" -> "textfield")
      } else {
        cleanAll
      }

    }
  }

  def renderForm = w(UserDoc.currentUser){u => w(urp.repo){repo => {
    if (!repo.forkOf.valueBox.isEmpty) {
        "button" #> SHtml.button(Text("new pull request"), processNewPullRequest(u), "class" -> "button", "id" -> "create_new_pull_request_button") &
          "name=description" #> SHtml.textarea(description, {
            value: String =>
              description = value.trim
          }, "placeholder" -> "Add a short description",
          "class" -> "textfield",
          "cols" -> "40", "rows" -> "20")
      }else {
        cleanAll
      }
      } 
    }
  }


  def processNewPullRequest(u : UserDoc)() = {
    val destHistory = destRepo.git.log(destRef).toList.reverse
    val srcHistory = sourceRepo.git.log(sourceRef).toList.reverse

    val diff = srcHistory.diff(destHistory)

    if(diff.isEmpty) {
      S.error("No new commits for destination repository")
    } else {
      PullRequestDoc.srcRepoId(sourceRepo.id.get)
          .destRepoId(destRepo.id.get)
          .srcRef(sourceRef)
          .destRef(destRef)
          .creatorId(u.id.get).description(description).save
        S.redirectTo(sourceRepo.pullRequestsUrl)
    }
  }

  def renderPullRequests = w(urp.repo){repo =>
   if(repo.pullRequests.isEmpty) 
    ".pull_request_list" #> "No pull requests for this repository."
   else
    ".pull_request_list" #> repo.pullRequests.map(pr => {
       ".pull_request [class+]" #> (if(pr.accepted_?.get) "closed_pr" else "opened_pr" ) &
       ".pull_request *" #> (
        ".from" #> a(pr.srcRepoId.obj.get.sourceTreeUrl(pr.srcRef.get), 
            Text(pr.srcRepoId.obj.get.owner.login.get + "/" + pr.srcRepoId.obj.get.name.get + "@" + pr.srcRef)) &
        ".to" #> a(pr.destRepoId.obj.get.sourceTreeUrl(pr.srcRef.get), 
            Text(pr.destRepoId.obj.get.owner.login.get + "/" + pr.destRepoId.obj.get.name.get + "@" + pr.destRef)) &
        ".whom" #> a(pr.creatorId.obj.get.homePageUrl, Text(pr.creatorId.obj.get.login.get)) &
        ".when" #> SnippetHelper.dateFormatter.format(pr.creationDate.get) &
        ".msg" #> a(pr.homePageUrl, Text(if(!pr.description.get.isEmpty) escape(pr.description.get) else "No description")))
    })
  }

}