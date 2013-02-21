/**
 * Mule Rest Module
 *
 * Copyright 2011-2012 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * This software is protected under international copyright law. All use of this software is
 * subject to MuleSoft's Master Subscription Agreement (or other master license agreement)
 * separately entered into in writing between you and MuleSoft. If such an agreement is not
 * in place, you may not use the software.
 */

package org.mule.module.apikit.rest.util;

import org.mule.module.apikit.rest.representation.RepresentationType;

import com.google.common.net.MediaType;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

/**
 * MIME-Type Parser
 * <p/>
 * This class provides basic functions for handling mime-types. It can handle
 * matching mime-types against a list of media-ranges. See section 14.1 of the
 * HTTP specification [RFC 2616] for a complete explanation.
 * <p/>
 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1
 * <p/>
 * A port to Java of Joe Gregorio's MIME-Type Parser:
 * <p/>
 * http://code.google.com/p/mimeparse/
 */
public final class RestContentTypeParser
{


    /**
     * Structure for holding a fitness/quality combo
     */
    protected static class FitnessAndQuality implements
            Comparable<FitnessAndQuality> {
        int fitness;

        float quality;

        String mimeType; // optionally used

        public FitnessAndQuality(int fitness, float quality) {
            this.fitness = fitness;
            this.quality = quality;
        }

        public int compareTo(FitnessAndQuality o) {
            if (fitness == o.fitness) {
                if (quality == o.quality)
                    return 0;
                else
                    return quality < o.quality ? -1 : 1;
            } else
                return fitness < o.fitness ? -1 : 1;
        }
    }

    /**
     * Find the best match for a given mimeType against a list of media_ranges
     * that have already been parsed by MimeParse.parseMediaRange(). Returns a
     * tuple of the fitness value and the value of the 'q' quality parameter of
     * the best match, or (-1, 0) if no match was found. Just as for
     * quality_parsed(), 'parsed_ranges' must be a list of parsed media ranges.
     *
     * @param mimeType
     * @param parsedRanges
     */
    protected static FitnessAndQuality fitnessAndQualityParsed(MediaType target,
                                                               List<MediaType> parsedRanges) {
        int bestFitness = -1;
        float bestFitQ = 0;

        for (MediaType range : parsedRanges) {
            if ((target.type().equals(range.type()) || range.type().equals("*") || target.type()
                    .equals("*"))
                    && (target.subtype().equals(range.subtype())
                    || range.subtype().equals("*") || target.subtype()
                    .equals("*"))) {
                for (String k : target.parameters().keySet()) {
                    int paramMatches = 0;
                    if (!k.equals("q") && range.parameters().containsKey(k)
                            && target.parameters().get(k).equals(range.parameters().get(k))) {
                        paramMatches++;
                    }
                    int fitness = (range.type().equals(target.type())) ? 100 : 0;
                    fitness += (range.subtype().equals(target.subtype())) ? 10 : 0;
                    fitness += paramMatches;
                    if (fitness > bestFitness) {
                        bestFitness = fitness;

                        if( range.type().equals("*") && range.subtype().equals("*")) {
                            bestFitQ = NumberUtils
                                    .toFloat(target.parameters().get("q").get(0), 0);
                        } else {
                        bestFitQ = NumberUtils
                                .toFloat(range.parameters().get("q").get(0), 0);
                        }
                    }
                }
            }
        }
        return new FitnessAndQuality(bestFitness, bestFitQ);
    }


    /**
     * Returns the quality 'q' of a mime-type when compared against the
     * mediaRanges in ranges. For example:
     *
     * @param mimeType
     */
    public static float quality(MediaType mimeType, List<MediaType> ranges) {
        return fitnessAndQualityParsed(mimeType, ranges).quality;
    }

    /**
     * Takes a list of supportedRepresentations mime-types and finds the best match for all the
     * media-ranges listed in header. The value of header must be a string that
     * conforms to the format of the HTTP Accept: header. The value of
     * 'supportedRepresentations' is a list of mime-types.
     * <p/>
     * MimeParse.bestMatch(Arrays.asList(new String[]{"application/xbel+xml",
     * "text/xml"}), "text/*;q=0.5,*; q=0.1") 'text/xml'
     *
     * @param supportedRepresentations
     * @param header
     * @return
     */
    public static MediaType bestMatch(Collection<RepresentationType> supportedRepresentations, List<MediaType> header) {
        List<FitnessAndQuality> weightedMatches = new LinkedList<FitnessAndQuality>();
        for (RepresentationType representation : supportedRepresentations) {
            FitnessAndQuality fitnessAndQuality = fitnessAndQualityParsed(representation.getMediaType(), header);
            fitnessAndQuality.mimeType = representation.getMediaType().toString();
            weightedMatches.add(fitnessAndQuality);
        }
        Collections.sort(weightedMatches);

        FitnessAndQuality lastOne = weightedMatches.get(weightedMatches.size() - 1);
        return MediaType.parse(NumberUtils.compare(lastOne.quality, 0) != 0 ? lastOne.mimeType : "");
    }

    // hidden
    private RestContentTypeParser() {
    }
}
