/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datumbox.framework.statistics.nonparametrics.relatedsamples;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.TransposeDataList;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.framework.statistics.descriptivestatistics.Ranks;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import java.util.Map;

/**
 * Wilcoxon's Related Samples non-parametric test.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class WilcoxonRelatedSamples {
    
    /**
     * Calculates the p-value of null Hypothesis.
     * 
     * @param transposeDataList
     * @return
     * @throws IllegalArgumentException 
     */
    public static double getPvalue(TransposeDataList transposeDataList) throws IllegalArgumentException {
        Object[] keys = transposeDataList.keySet().toArray();
        if(keys.length!=2) {
            throw new IllegalArgumentException();
        }
        
        Object keyX = keys[0];
        Object keyY = keys[1];
        
        FlatDataList flatDataListX = transposeDataList.get(keyX);
        FlatDataList flatDataListY = transposeDataList.get(keyY);

        int n = flatDataListX.size();
        if(n<=0 || n!=flatDataListY.size()) {
            throw new IllegalArgumentException();
        }

        AssociativeArray Di = new AssociativeArray();
        for(int j=0;j<n;++j) {
            double delta= flatDataListX.getDouble(j) - flatDataListY.getDouble(j);

            if(delta==0) {
                continue; //don't count it at all
            }

            String key="+";
            if(delta<0) {
                key="-";
            }
            Di.put(key+Integer.toString(j), Math.abs(delta));
        }

        //converts the values of the table with its Ranks
        Ranks.getRanksFromValues(Di);
        double W=0;
        for(Map.Entry<Object, Object> entry : Di.entrySet()) {
            if(entry.getKey().toString().charAt(0)=='+') {
                W+=TypeInference.toDouble(entry.getValue());
            }
        }

        double pvalue = scoreToPvalue(W, n);

        return pvalue;
    }

    /**
     * Tests the rejection of null Hypothesis for a particular confidence level 
     * 
     * @param transposeDataList
     * @param is_twoTailed
     * @param aLevel
     * @return
     * @throws IllegalArgumentException 
     */
    public static boolean test(TransposeDataList transposeDataList, boolean is_twoTailed, double aLevel) throws IllegalArgumentException {   
        if(transposeDataList.size()!=2) {
            throw new IllegalArgumentException();
        }

        double pvalue= getPvalue(transposeDataList);

        boolean rejectH0=false;

        double a=aLevel;
        if(is_twoTailed) { //if to tailed test then split the statistical significance in half
            a=aLevel/2;
        }
        if(pvalue<=a || pvalue>=(1-a)) {
            rejectH0=true; 
        }

        return rejectH0;
    }
    
    /**
     * Returns the Pvalue for a particular score
     * 
     * @param score
     * @param n
     * @return 
     */
    protected static double scoreToPvalue(double score, int n) {
        if(n<=20) {
            //calculate it from binomial distribution
            //EXPAND: waiting for tables from Dimaki
        }

        double mean=n*(n+1.0)/4.0;
        double variable=n*(n+1.0)*(2.0*n+1.0)/24.0;

        double z=(score-mean)/Math.sqrt(variable);

        return ContinuousDistributions.GaussCdf(z);
    }
}
