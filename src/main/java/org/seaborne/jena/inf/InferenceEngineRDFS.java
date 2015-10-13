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

import java.util.Set ;

import org.apache.jena.graph.Node ;
import org.apache.jena.vocabulary.RDF ;
import org.apache.jena.vocabulary.RDFS ;

/**
 * Apply a fixed set of inference rules to a stream of triples. This is
 * inference on the A-Box (the data) with respect to a fixed T-Box (the
 * vocabulary, ontology).
 * <ul>
 * <li>rdfs:subClassOf (transitive)</li>
 * <li>rdfs:subPropertyOf (transitive)</li>
 * <li>rdfs:domain</li>
 * <li>rdfs:range</li>
 * </ul>
 * 
 * Usage: call process(Node, Node, Node), outputs to derive(Node, Node, Node).
 */

public class InferenceEngineRDFS {
    // Todo:
    // rdfs:member
    // list:member ???

    static final Node rdfType           = RDF.type.asNode() ;
    static final Node rdfsSubClassOf    = RDFS.subClassOf.asNode() ;
    static final Node rdfsSubPropertyOf = RDFS.subPropertyOf.asNode() ;
    static final Node rdfsDomain        = RDFS.domain.asNode() ;
    static final Node rdfsRange         = RDFS.range.asNode() ;

    private final InferenceSetupRDFS setup ;
    private final SinkTriple dest ;

    public InferenceEngineRDFS(InferenceSetupRDFS state, SinkTriple dest) {
        this.setup = state ;
        this.dest = dest ;
    }

    public void process(Node s, Node p, Node o) {
        subClass(s, p, o) ;
        subProperty(s, p, o) ;

        // domain() and range() also go through subClass processing.
        domain(s, p, o) ;
        range(s, p, o) ;
    }

    /** Any triple derived is sent to this method - does not include the trigger triple
     * (but that might be derived as well as being concrete). 
     */
    protected void derive(Node s, Node p, Node o) {
        dest.receive(s, p, o) ;
    }

    // Rule extracts from Jena's RDFS rules etc/rdfs.rules

    /*
     * [rdfs8: (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c) -> (?a rdfs:subClassOf ?c)]
     * [rdfs9: (?x rdfs:subClassOf ?y), (?a rdf:type ?x) -> (?a rdf:type ?y)]
     */
    final private void subClass(Node s, Node p, Node o) {
        if ( p.equals(rdfType) ) {
            Set<Node> x = setup.getSuperClasses(o) ;
            x.forEach(c -> derive(s, rdfType, c)) ;
            if ( setup.includeDerivedDataRDFS() ) {
                subClass(o, rdfsSubClassOf, o) ;    // Recurse
            }
        }
        if ( setup.includeDerivedDataRDFS() && p.equals(rdfsSubClassOf) ) {
            Set<Node> superClasses = setup.getSuperClasses(o) ;
            superClasses.forEach(c -> derive(o, p, c)) ;
            Set<Node> subClasses = setup.getSubClasses(o) ;
            subClasses.forEach(c -> derive(c, p, o)) ;
            derive(s, p, s) ;
            derive(o, p, o) ;
        }
    }

    /*
     * [rdfs5a: (?a rdfs:subPropertyOf ?b), (?b rdfs:subPropertyOf ?c) -> (?a rdfs:subPropertyOf ?c)] 
     * [rdfs6: (?a ?p ?b), (?p rdfs:subPropertyOf ?q) -> (?a ?q ?b)]
     */
    private void subProperty(Node s, Node p, Node o) {
        Set<Node> x = setup.getSuperProperties(p) ;
        x.forEach(p2 -> derive(s, p2, o)) ;
        if ( setup.includeDerivedDataRDFS() ) {
            if ( ! x.isEmpty() )
                subProperty(p, rdfsSubPropertyOf, p) ;
            if ( p.equals(rdfsSubPropertyOf) ) {
                // ** RDFS extra
                Set<Node> superProperties = setup.getSuperProperties(o) ;
                superProperties.forEach( c -> derive(o, p, c)) ;
                Set<Node> subProperties = setup.getSubProperties(o) ;
                subProperties.forEach(c -> derive(c, p, o)) ;
                derive(s, p, s) ;
                derive(o, p, o) ;
            }
        }
    }

    /*
     * [rdfs2: (?p rdfs:domain ?c) -> [(?x rdf:type ?c) <- (?x ?p ?y)] ]
     * [rdfs9: (?x rdfs:subClassOf ?y), (?a rdf:type ?x) -> (?a rdf:type ?y)]
     */
    final private void domain(Node s, Node p, Node o) {
        Set<Node> x = setup.getDomain(p) ;
        x.forEach(c -> {
            derive(s, rdfType, c) ;
            subClass(s, rdfType, c) ;
            if ( setup.includeDerivedDataRDFS() )
                derive(p, rdfsDomain, c) ;
        }) ;
    }

    /*
     * [rdfs3: (?p rdfs:range ?c) -> [(?y rdf:type ?c) <- (?x ?p ?y)] ]
     * [rdfs9: (?x rdfs:subClassOf ?y), (?a rdf:type ?x) -> (?a rdf:type ?y)]
     */
    final private void range(Node s, Node p, Node o) {
        // Mask out literal subjects
        if ( o.isLiteral() )
            return ;
        // Range
        Set<Node> x = setup.getRange(p) ;
        x.forEach(c -> {
            derive(o, rdfType, c) ;
            subClass(o, rdfType, c) ;
            if ( setup.includeDerivedDataRDFS() )
                derive(p, rdfsRange, c) ;
        }) ;
    }
}

