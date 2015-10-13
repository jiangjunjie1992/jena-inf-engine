/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.jena.inf ;

import org.apache.jena.riot.system.StreamRDF ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.rdf.model.Model ;

public class InfFactory {
    
    public static Graph graphRDF(Graph data, Graph vocab) {
        return graphRDF(data, new InferenceSetupRDFS(vocab)) ;
    }

    public static Graph graphRDF(Graph data, InferenceSetupRDFS setup) {
        return new GraphRDFS(setup, data) ;
    }

    public static StreamRDF inf(StreamRDF data, Model vocab) {
        InferenceSetupRDFS setup = new InferenceSetupRDFS(vocab) ;
        return inf(data, setup) ;
    }

    public static StreamRDF inf(StreamRDF data, InferenceSetupRDFS setup) {
        InferenceProcessorRDFS inf = new InferenceProcessorRDFS(setup) ;
        return null ; // inf.process(null)
    }
}
