/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import net.liftweb._
import common.Full
import http.SHtml
import util.Helpers._
import SnippetHelper._
import bootstrap.liftweb.UserRepoCommitPage
import util.PassThru
import xml.Text
import code.model._

/**
 * User: denis.bardadym
 * Date: 10/5/11
 * Time: 2:14 PM
 */

class RepoCommitOps(urp: UserRepoCommitPage) {

   def renderSourcesLink = urp.repo match {
       case Full(repo) =>  "*" #> a(repo.sourceTreeUrl, Text("Sources"))
     case _ => PassThru
  }

  def renderCommitsLink = urp.repo match {
        case Full(repo) =>  "*" #> a(repo.commitsUrl, Text("Commits"))
      case _ => PassThru
   }

  def renderCurrentSourcesLink = urp.repo match {
        case Full(repo) =>  "*" #> a(repo.sourceTreeUrl(urp.commit), Text("Tree"))
      case _ => PassThru
   }


  def renderCommitsList = urp.repo match {
       case Full(repo) =>
      ".commits_list *" #> groupCommitsByDate(repo.git.log(urp.commit)).map(p => {
        <div class="day">
          <h3 class="date">
            {p._1}
          </h3>{p._2.map(lc => {
          <div class="commit">
            <pre class="commit_msg">
              {lc.getFullMessage}
            </pre>

            <p class="commit_author">
              {lc.getAuthorIdent.getName}
              at
              {SnippetHelper.timeFormatter.format(lc.getAuthorIdent.getWhen)}
            </p>
            <span class="source_tree_link">
              {a(repo.sourceTreeUrl(lc.getName), Text("Source tree"))}
            </span>
            <br/>
            <span class="diff_link">
              {a(repo.commitUrl(lc.getName), Text(lc.getName))}
            </span>


          </div>
        })}

        </div>
      })
      case _ => PassThru
  }


  def renderBranchSelector = branchSelector(urp.repo, _ => urp.commit, _.commitsUrl(_))

  def renderDiffList =  urp.repo match {


        case Full(repo) => {
          val diff = repo.git.diff(urp.commit)

          ".diff_status_list *" #> (diff._1.map(
            _ match {
              case  Added(p ) => <li class="new">{p}</li>
              case  Deleted(p) => <li class="deleted">{p}</li>
              case  Modified(p) => <li class="modified">{p}</li>
              case  Copied(op, np) => <li class="modified">{op} -> {np}</li>
              case  Renamed(op, np) => <li class="modified">{op} -> {np}</li>
            }
          )) & ".diff_list *" #> (diff._2.flatMap(d => {
            <div class="source_code_holder">
            <pre>
              <code>{d}</code>
              </pre>
            </div>
          }) )
        }
      case _ => PassThru
   }



}