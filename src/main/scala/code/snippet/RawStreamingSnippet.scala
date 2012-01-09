/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package code.snippet

import net.liftweb._
import http._
import common._
import util._
import Helpers._
import code.model._
import code.snippet.SnippetHelper._
import bootstrap.liftweb._
import xml._
import Utility._
import com.foursquare.rogue.Rogue._
import net.liftweb.http.rest._

object RawFileStreamingSnippet extends Loggable with RestHelper {


   serve {
    case Req(user :: repo :: "raw" :: ref :: path, //  path
             _, // suffix
             GetRequest) =>
      {
      	(UserDoc where (_.login eqs user) get).flatMap(u => tryo {u.repos.filter(_.name.get == repo).head}) match {
      		case Some(r) if(r.canPush_?(UserDoc.currentUser) || r.open_?.get) => {

      			

      			val reversedPath = path.reverse
				
				(tryo { r.git.ls_tree(reversedPath.tail.reverse, ref).filter(_.basename == reversedPath.head).head } or {Empty}) match {
					
					case Full(b @ Blob(_, size)) => {
						r.git.withSourceElementStream(path, ref) { in => 
							StreamingResponse(in, () => {in.close()}, size, List("Content-Type" -> (if (b.image_?) "image/" + b.extname else if(b.binary_?) "application/octet-stream" else "text/plain"),
						        "Content-Disposition" -> ("attachment; filename=export" + b.extname)), Nil, 200)
						    }
					}
						
					
					case _ => InMemoryResponse("".getBytes, List("Content-Type" -> "text/plain"), Nil, 404)
				}
      		}
      		case _ => InMemoryResponse("".getBytes, List("Content-Type" -> "text/plain"), Nil, 404)
      	}
      
   	}
	}

}