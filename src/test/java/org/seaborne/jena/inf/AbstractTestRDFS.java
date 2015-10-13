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

import static org.seaborne.jena.inf.InfGlobal.rdfType ;

import java.io.IOException ;
import java.io.PrintStream ;
import java.util.Collection ;
import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.SetUtils ;
import org.junit.Assert ;
import org.junit.Test ;
import org.seaborne.jena.inf.InfGlobal ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.rdf.model.InfModel ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.reasoner.Reasoner ;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner ;
import org.apache.jena.reasoner.rulesys.Rule ;
import org.apache.jena.util.FileUtils ;

public abstract class AbstractTestRDFS extends BaseTest {
    private static PrintStream      out = System.err ;
    
    static Node node(String str) { return NodeFactory.createURI("http://example/"+str) ; }

    // XXX Check this is coverage.
    
    @Test public void test_rdfs_01()       { test(node("a"), rdfType, null) ; }
    @Test public void test_rdfs_02()       { test(node("a"), rdfType, node("T2")) ; }
    @Test public void test_rdfs_03()       { test(null, rdfType, node("T2")) ; }

    @Test public void test_rdfs_04()       { test(null, null, node("T2")) ; }
    @Test public void test_rdfs_05()       { test(null, rdfType, node("T")) ; }
    @Test public void test_rdfs_05a()      { test(null, null, node("T")) ; }
      
    @Test public void test_rdfs_06()       { test(node("c"), rdfType, null) ; }
    @Test public void test_rdfs_06a()      { test(node("c"), null, null) ; }
      
    @Test public void test_rdfs_07()       { test(null, rdfType, null) ; }
    @Test public void test_rdfs_08()       { test(null, node("q"), null) ; }
      
    @Test public void test_rdfs_08a()       { test(null, node("p"), null) ; }
    @Test public void test_rdfs_08b()       { test(null, node("pp"), null) ; }
    @Test public void test_rdfs_08c()       { test(null, node("ppp"), null) ; }
    @Test public void test_rdfs_08d()       { test(null, node("pTop"), null) ; }
    
    @Test public void test_rdfs_09()       { test(node("z"), null, null) ;  }
    @Test public void test_rdfs_10()       { test(node("z"), rdfType, null) ; }
      
    @Test public void test_rdfs_11()       { test(null, null, null) ; }
      
    @Test public void test_rdfs_12a()       { test(null, rdfType, node("P")) ; }
    @Test public void test_rdfs_12b()       { test(null, rdfType, node("P1")) ; }
    @Test public void test_rdfs_12c()       { test(null, rdfType, node("P2")) ; }
    @Test public void test_rdfs_12d()       { test(null, null, node("P")) ; }
    @Test public void test_rdfs_12e()       { test(null, null, node("P1")) ; }
    @Test public void test_rdfs_12f()       { test(null, null, node("P2")) ; }

    @Test public void test_rdfs_13a()       { test(null, rdfType, node("Q")) ; }
    @Test public void test_rdfs_13b()       { test(null, rdfType, node("Q1")) ; }
    @Test public void test_rdfs_13c()       { test(null, rdfType, node("Q2")) ; }
    @Test public void test_rdfs_13d()       { test(null, null, node("Q")) ; }
    @Test public void test_rdfs_13e()       { test(null, null, node("Q1")) ; }
    @Test public void test_rdfs_13f()       { test(null, null, node("Q2")) ; }

    // all T cases.
    // all U cases.
    @Test public void test_rdfs_14a()       { test(null, rdfType, node("T")) ; }
    @Test public void test_rdfs_14b()       { test(null, rdfType, node("T1")) ; }
    @Test public void test_rdfs_14c()       { test(null, rdfType, node("S2")) ; }
    @Test public void test_rdfs_14d()       { test(null, null, node("T")) ; }
    @Test public void test_rdfs_14e()       { test(null, null, node("T1")) ; }
    @Test public void test_rdfs_14f()       { test(null, null, node("S2")) ; }

    @Test public void test_rdfs_15a()       { test(null, rdfType, node("U")) ; }
    @Test public void test_rdfs_15b()       { test(null, null, node("U")) ; }
    
    @Test public void test_rdfs_16a()       { test(null, null, node("X")) ; }
    @Test public void test_rdfs_16b()       { test(null, rdfType, node("X")) ; }
    
    @Test public void test_rdfs_20()        { test(null, node("p"), null) ; }
    @Test public void test_rdfs_21()        { test(null, node("pp"), null) ; }
    @Test public void test_rdfs_22()        { test(null, node("ppp"), null) ; }
    
    @Test public void test_rdfs_30()        { test(node("e"), null, null) ; }
    @Test public void test_rdfs_31()        { test(node("e"), node("r"), null) ; }
    
    protected void test(Node s, Node p, Node o) {
        Graph refGraph = getReferenceGraph() ;
        
        List<Triple> x0 = refGraph.find(s,p,o).toList() ;
        
        if ( removeVocabFromReferenceResults() )
            x0 = InfGlobal.removeRDFS(x0) ;

        List<Triple> x1 = findInTestGraph(s,p,o) ;
        
        if ( ! InfTestLib.sameElts(x0, x1) ) {
            out.println("Expected: find("+s+", "+p+", "+o+")") ;
            print(out, x0) ;
            out.println("Got ("+getTestLabel()+"):") ;
            print(out, x1) ;
            out.println() ;
            out.println("Diff:") ;
            printDiff(out, Iter.toSet(x0.iterator()), Iter.toSet(x1.iterator())) ;
        }
        
        Assert.assertTrue(getTestLabel(), InfTestLib.sameElts(x0, x1)) ;
    }

    protected List<Triple> findInTestGraph(Node s, Node p, Node o) {
        return getTestGraph().find(s,p,o).toList() ;
    }

    protected static Graph createRDFSGraph(Model data, Model vocab) {
        return ModelFactory.createRDFSModel(vocab, data).getGraph() ;
    }
    
    protected static Graph createRulesGraph(Model data, Model vocab, String rulesFile) {
        try {
            String rules = FileUtils.readWholeFileAsUTF8(rulesFile) ;
            rules = rules.replaceAll("#[^\\n]*", "") ;
            Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules)) ;
            // Model m = ModelFactory.createInfModel(reasoner, data);
            /** Rules way */
            InfModel m = ModelFactory.createInfModel(reasoner, vocab, data) ;
            return m.getGraph() ;
        }
        catch (IOException ex) { IO.exception(ex) ; return null ; }
    }

    
    protected boolean removeVocabFromReferenceResults()      { return true ; } 
    protected abstract Graph getReferenceGraph() ;
    protected abstract Graph getTestGraph() ;
    protected abstract String getReferenceLabel() ;
    protected abstract String getTestLabel() ;

    static protected <X> void printDiff(PrintStream out, Set<X> A, Set<X> B) {
        SetUtils.difference(A, B).stream().forEach(item -> out.println("> "+item)) ;    
        SetUtils.difference(B, A).stream().forEach(item -> out.println("< "+item)) ;
    }
    
    static protected void print(PrintStream out, Collection<Triple> x) {
        print(out, "  ", x) ;
    }
    
    static protected void print(PrintStream out, String leader, Collection<Triple> x) {
        if ( x.isEmpty() )
            out.println(leader+"{}") ;
        else
            x.stream().forEach(triple -> {out.println(leader+triple) ; }) ;
    }

}

