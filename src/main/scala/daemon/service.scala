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
package daemon

import code.model._
import java.io._
import net.liftweb._
import common._
import util._

import org.eclipse.jgit.transport._
import org.eclipse.jgit.transport.RefAdvertiser.PacketLineOutRefAdvertiser
import org.eclipse.jgit.revwalk.RevWalk

import java.io._

trait Service {

	def shutdown(): Unit

	def init(): Unit
}

trait Pack {
  val repo: RepositoryDoc

  def sendInfoRefs(out: PacketLineOut): Unit

  def sendPack(in: InputStream, out: OutputStream, err: OutputStream): Unit
}

class UploadPack(val repo: RepositoryDoc, twoWay: Boolean = true) extends Pack with Loggable {
  private val p = repo.git.upload_pack
  p.setBiDirectionalPipe(twoWay)

  def sendInfoRefs(out: PacketLineOut) {
    try {               
      p.sendAdvertisedRefs(new PacketLineOutRefAdvertiser(out))
    } catch {
      case e: IOException => logger.warn("IOException: %s".format(e.getMessage))
    } finally {
      p.getRevWalk.release
    }
  }

  def sendPack(in: InputStream, out: OutputStream, err: OutputStream) {
    try {
      p.upload(in, out, err)
    } catch {
      case e: IOException => logger.warn("IOException: %s".format(e.getMessage))
    } 
  }
}

class ReceivePack(val repo: RepositoryDoc, twoWay: Boolean = true) extends Pack with Loggable {
  private val p = repo.git.receive_pack
  p.setBiDirectionalPipe(twoWay)

  def sendInfoRefs(out: PacketLineOut) {
    try {               
      p.sendAdvertisedRefs(new PacketLineOutRefAdvertiser(out))
    } catch {
      case e: IOException => logger.warn("IOException: %s".format(e.getMessage))
    } finally {
      p.getRevWalk.release
    }
  }

  def sendPack(in: InputStream, out: OutputStream, err: OutputStream) {
    import scala.collection.JavaConversions._
    import notification.client._

    val oldHeads = repo.git.refsHeads.map(ref => (ref.getName, ref)).toMap
    val ident = p.getRefLogIdent //TODO fill this
 
    try {
      p.receive(in, out, err)
      NotifyActor ! PushEvent(repo, ident, oldHeads)
    } catch {
      case e: IOException => logger.warn("IOException: %s".format(e.getMessage))
    }
  }
}

trait Resolver {

  val GIT_UPLOAD_PACK = "git-upload-pack"

  val GIT_RECEIVE_PACK = "git-receive-pack"

  type packProcessor = (InputStream, OutputStream, OutputStream) => Unit

  def packProcessing(r: Box[RepositoryDoc], 
                     processor: (RepositoryDoc) => packProcessor,
                     accessible: (RepositoryDoc) => Boolean = (r) => {true}):
      Box[packProcessor] = {
    for{repo <- r; if(accessible(repo))} yield {
       processor(repo)
    }
  }  

  def repoByPath(arg: String, user: Option[UserDoc] = None) = {
    arg match { 
      case Repo1(userName, repoName) => RepositoryDoc.byUserLoginAndRepoName(userName, repoName)
  
      case Repo2(repoName) => user.flatMap(_.repos.filter(_.name.get == repoName).headOption)
  
      case _ => None
    }
  }

  def uploadPack(r: RepositoryDoc) = new UploadPack(r).sendPack _

  def receivePack(r: RepositoryDoc) = new ReceivePack(r).sendPack _

  val ident = """[0-9a-zA-Z\.-]+"""
  
  val Repo1 = """'?/?(%s)/(%s)\.git'?""".format(ident, ident).r
  val Repo2 =  """'?/?(%s)\.git'?""".format(ident).r
}