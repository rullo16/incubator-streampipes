/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {PlatformServicesCommons} from "./commons.service";
import {Observable} from "rxjs";
import {
    Message, NodeInfoDescription,
} from "../../core-model/gen/streampipes-model";
import {map} from "rxjs/operators";

@Injectable()
export class NodeService {

    constructor(private http: HttpClient,
                private platformServicesCommons: PlatformServicesCommons) {
    }

    getNodes(): Observable<NodeInfoDescription[]> {
        return this.http.get(this.platformServicesCommons.authUserBasePath() + "/nodes")
            .pipe(map(response => {
                return response as NodeInfoDescription[];
            }));
    }

    getOnlineNodes(): Observable<NodeInfoDescription[]> {
        return this.http.get(this.platformServicesCommons.authUserBasePath() + "/nodes/online")
            .pipe(map(response => {
                return response as NodeInfoDescription[];
            }));
    }

    updateNodeState(node: NodeInfoDescription): Observable<Message> {
        return this.http.put(this.platformServicesCommons.authUserBasePath() + '/nodes/' + node.nodeControllerId, node)
            .pipe(map(response => {
                return Message.fromData(response as Message);
            }));
    }

    activateNode(nodeControllerId: string): Observable<Message> {
        return this.http.post(this.platformServicesCommons.authUserBasePath() + '/nodes/active/' + nodeControllerId, {})
            .pipe(map(response => {
                return Message.fromData(response as Message);
            }));
    }

    deactivateNode(nodeControllerId: string): Observable<Message> {
        return this.http.post(this.platformServicesCommons.authUserBasePath() + '/nodes/inactive/' + nodeControllerId, {})
            .pipe(map(response => {
                return Message.fromData(response as Message);
            }));
    }
}