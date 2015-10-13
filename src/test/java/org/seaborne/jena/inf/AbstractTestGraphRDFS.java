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

package org.seaborne.jena.inf;

import org.apache.jena.riot.RDFDataMgr ;
import org.junit.BeforeClass ;
import org.seaborne.jena.inf.InferenceSetupRDFS ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.rdf.model.Model ;

/** Test of RDFS, with separate data and vocabulary, no RDFS in the deductions. */
public abstract class AbstractTestGraphRDFS extends AbstractTestRDFS {
    protected static Model vocab ;
    protected static Model data ;

    static InferenceSetupRDFS setup ;
    // Jena graph to check results against.
    static Graph infGraph ;
    // The main test target
    static Graph testGraphRDFS ;
    
    static final String DIR = "testing/Inf" ;
    static final String DATA_FILE = DIR+"/rdfs-data.ttl" ;
    static final String VOCAB_FILE = DIR+"/rdfs-vocab.ttl" ;
    //static final String RULES_FILE = DIR+"/rdfs-min-backwards.rules" ;
    static final String RULES_FILE = DIR+"/rdfs-min.rules" ;
    
    @BeforeClass public static void setupClass() {
        vocab = RDFDataMgr.loadModel(VOCAB_FILE) ;
        data = RDFDataMgr.loadModel(DATA_FILE) ;
        setup = new InferenceSetupRDFS(vocab) ;
        infGraph = createRulesGraph(data, vocab, RULES_FILE) ;
    }
    
    protected AbstractTestGraphRDFS() {
        testGraphRDFS = createGraphRDFS(setup, data.getGraph()) ;
    }
    
    protected abstract Graph createGraphRDFS(InferenceSetupRDFS setup, Graph data) ; 

    @Override
    protected Graph getReferenceGraph() {
        return infGraph ;
    }

    @Override
    protected Graph getTestGraph() {
        return testGraphRDFS ;
    }

    @Override
    protected String getReferenceLabel() {
        return "Inference" ;
    }

    @Override
    protected String getTestLabel() {
        return "GraphRDFS" ;
    }
}

