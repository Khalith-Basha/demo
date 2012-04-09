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
package notification.client

import net.liftweb._
import util._
import common._

import actor.{LiftActor, ActorLogger}

import code.model._
import NotifyEvents._

import org.eclipse.jgit._
import revwalk.RevCommit
import lib._

import dispatch._

import json.JsonDSL._
import json.JsonAST._
import json.Printer._




object NotifyActor extends LiftActor {
	lazy val notifyServerUrl = Props.get("notification.url")

	private def subscribers(t: NotifyEvents.Value, repo: RepositoryDoc) = {
		import com.foursquare.rogue.Rogue._

		for { subscription <- (NotifySubscriptionDoc where (_.repo eqs repo.id.get) and (_.onWhat eqs t) fetch) 
						user <- subscription.who.obj} 
				yield {ActorLogger.debug(subscription); (user, subscription.output.get)}
	}

	implicit def asJValue(pi: PersonIdent): JValue = {
		if(pi == null) JObject(Nil)
		else JObject(
				JField("name", pi.getName) :: 
				JField("email", pi.getEmailAddress) :: 
				Nil)
	}

	implicit def asJValue(cl: List[RevCommit]): JValue = {
		import org.joda.time.DateTime

		JArray(cl.map(commit => 
			JObject(
				JField("message", commit.getFullMessage) :: 
				JField("author", commit.getAuthorIdent) :: 
				JField("date", (new DateTime(commit.getAuthorIdent.getWhen)).toString) :: 
				Nil).asInstanceOf[JValue]))
		}
	
	implicit def asJValue(m: Map[String, Ref]): JValue = {
		JArray(m.map(entry => 
			JString(entry._1).asInstanceOf[JValue]).toList)
	}


	def messageHandler = {
		case PushEvent(repo, ident, oldRefs) =>

			notifyServerUrl.map(urlAddress => {
				val subs = subscribers(NotifyEvents.Push, repo)

				if(!subs.isEmpty) {

					val newRefs = repo.git.refsHeads.map(ref => (ref.getName, ref)).toMap

					val (changedBranchesOld, deletedBranches) = oldRefs.partition(r => newRefs.contains(r._1))//(changed, deleted)
					val (changedBranchesNew, newBranches) = newRefs.partition(r => oldRefs.contains(r._1))//(changed, new)

					val changedHistory = for{(s, oldRef) <- changedBranchesOld
						newRef <- changedBranchesNew.get(s)
						if(oldRef.getObjectId != newRef.getObjectId)} yield 
							(s, repo.git.log(oldRef.getObjectId, newRef.getObjectId).toList)
					
					if(!newBranches.isEmpty || !deletedBranches.isEmpty || !changedHistory.isEmpty) {//we need to send something
								
						for((user, output) <- subs) {

							h((url(urlAddress + "/push") <<< pretty(render(
								("services", output.asJValue) ~
								("repository", repo.asJValue) ~
								("newBranches",newBranches) ~
								("deletedBranches", deletedBranches) ~
								("changedHistory", changedHistory) ~
								("gitPusher", ident)
							))) >|)
						}
					}
				}
				
			})			
	}

	lazy val h = new nio.Http

	def onShutdown() = {
		h.shutdown
	}


}