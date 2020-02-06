/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package eu.fasten.analyzer.javacgwala;

import eu.fasten.analyzer.javacgwala.data.callgraph.WalaCallGraph;

import eu.fasten.analyzer.javacgwala.data.fastenjson.CanonicalJSON;
import eu.fasten.analyzer.javacgwala.data.type.MavenResolvedCoordinate;
import eu.fasten.analyzer.javacgwala.generator.WalaCallgraphConstructor;
import eu.fasten.analyzer.javacgwala.generator.WalaUFIAdapter;
import eu.fasten.core.data.RevisionCallGraph;
import eu.fasten.core.plugins.FastenPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WalaJavaCGGen implements FastenPlugin {

    private static Logger logger = LoggerFactory.getLogger(WalaJavaCGGen.class);

    @Override
    public String name() {
        return "eu.fasten.analyzer.javacgwala";
    }

    @Override
    public String description() {
        return "Constructs call graphs for Java packages using Wala.";
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    /**
     * Generates a call graph using Wala analyzer.
     *
     * @param coordinate - Maven coordinate
     * @return - call graph in FASTEN compatible format
     */
    public static RevisionCallGraph generateCallGraph(String coordinate) {

        try {
            List<MavenResolvedCoordinate> path = buildClasspath(coordinate);
            logger.debug("Building generator using generator....");
            long start = System.currentTimeMillis();
            WalaCallGraph cg = WalaCallgraphConstructor.build(path);
            logger.debug("Call graph construction took {}ms", System.currentTimeMillis() - start);

            //TODO: figure out what date variable should be
            return CanonicalJSON.toJsonCallgraph(WalaUFIAdapter.wrap(cg), 0);
        } catch (Exception e) {
            logger.error("An exception occurred for {}", coordinate, e);
            return null;
        }
    }

    /**
     * Build a class path for given maven coordinate.
     *
     * @param mavenCoordinate - maven coordinate
     * @return - list of resolved maven coordinates
     */
    private static List<MavenResolvedCoordinate> buildClasspath(String mavenCoordinate) {
        logger.debug("Building classpath for {}", mavenCoordinate);
        var artifacts = Maven.resolver()
                .resolve(mavenCoordinate)
                .withTransitivity()
                .asResolvedArtifact();

        var paths = Arrays.stream(artifacts)
                .map(MavenResolvedCoordinate::of)
                .collect(Collectors.toList());
        logger.debug("The classpath for {} is {}", mavenCoordinate, paths);
        return paths;
    }
}

