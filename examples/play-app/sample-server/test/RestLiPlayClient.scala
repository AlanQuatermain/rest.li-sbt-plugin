/*
   Copyright (c) 2014 LinkedIn Corp.

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

import com.linkedin.common.util.None
import com.linkedin.r2.transport.common.Client
import com.linkedin.restli.client.{Request, Response, RestClient}
import scala.concurrent.Future

/**
  * @author jbetz@linkedin.com
  */
class RestLiPlayClient(restClient: RestClient) {
  def this(client: Client, uriPrefix: String) = this(new RestClient(client, uriPrefix))

  def sendRequest[T](request: Request[T]): Future[Response[T]] = {
    val callback = new CallbackPromiseAdapter[Response[T]]
    restClient.sendRequest(request, callback)
    callback.promise.future
  }

  def shutdown(): Future[None] = {
    val callback = new CallbackPromiseAdapter[None]
    restClient.shutdown(callback)
    callback.promise.future
  }
}
