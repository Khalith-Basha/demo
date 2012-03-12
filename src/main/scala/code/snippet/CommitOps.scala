/*
   Copyright 2012 Denis Bardadym

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package code.snippet

import net.liftweb._
import common._
import http._
import util.Helpers._
import SnippetHelper._
import bootstrap.liftweb._
import util._
import xml._
import Utility._
import code.model._
import code.lib._

/**
 * User: denis.bardadym
 * Date: 10/5/11
 * Time: 2:14 PM
 */

class CommitOps(c: SourceElementPage) {

  def renderSourceTreeDefaultLink = w(c.repo)(renderSourceTreeLink(_, Full(c.commit)))

  def renderCommitsDefaultLink = w(c.repo)(renderCommitsLink(_, Full(c.commit)))

  def renderBranchSelector = w(c.repo){repo => 
    if(c.path.isEmpty)
    ".current_branch" #>
          SHtml.ajaxSelect(repo.git.branches.zip(repo.git.branches),
            if(repo.git.branches.contains(c.commit)) Full(c.commit) else Empty,
            value => S.redirectTo(Sitemap.historyAtCommit.calcHref(SourceElementPage(repo,value))))
    else cleanAll
  }


  def renderCommitsList = w(c.repo){repo => 
        ".day *" #>  groupCommitsByDate(repo.git.log(c.commit, c.path.mkString("/"))).map(p => {
        ".date *" #> p._1 &
        ".commit *" #> p._2.map(lc => {
          ".commit_msg *" #> <span>{lc.getFullMessage.split("\n").map(m => <span>{m}</span><br/>)}</span> &
          ".commit_author *" #> (lc.getAuthorIdent.getName + " at " + timeFormat(lc.getAuthorIdent.getWhen)) &
          ".source_tree_link *" #> {
            val sep = c.copy(commit = lc.getName)
            c.elem match {
              case Full(Tree(_)) => a(Sitemap.treeAtCommit.calcHref(sep), Text("tree"))
              case Full(Blob(_, _)) => a(Sitemap.blobAtCommit.calcHref(sep), Text("blob"))
              case _ => NodeSeq.Empty
            }} &
          ".diff_link *" #> a(Sitemap.commit.calcHref(c.copy(commit = lc.getName)), Text(lc.getName))
          
      })
    })  
  }

  def renderDiffList =  w(c.repo){repo => {
          val diff = repo.git.diff(c.commit + "^1", c.commit, Some(c.path.mkString("/")))

          val diffCount = diff.size // bad

          ".blob *" #> (diff.zipWithIndex.map(d => 
              (".blob_header [id]" #> ("diff" + d._2) &
                ".source_code" #> d._1._2 & 
              ".blob_header *" #> ((d._1._1 match {
                                            case  Added(p ) => (".status [class+]" #> "new" & ".status *" #> p)
                                            case  Deleted(p) => (".status [class+]" #> "deleted" & ".status *" #> p)
                                            case  Modified(p) => (".status [class+]" #> "modified" & ".status *" #> p)
                                            case  Copied(op, np) => (".status [class+]" #> "modified" & ".status *" #> (op + " -> " + np))
                                            case  Renamed(op, np) => (".status [class+]" #> "modified" & ".status *" #> (op + " -> " + np))
                                          }) &
                            ".prev [href]" #> (if(0 <= d._2 - 1)"#diff" + (d._2 - 1) else "") &
                            ".next [href]" #> (if(diffCount > d._2 + 1)"#diff" + (d._2 + 1) else "") ))
          ))
        }
   }
}
