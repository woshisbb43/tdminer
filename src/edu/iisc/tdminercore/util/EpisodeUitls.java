/*
 * EpisodeUitls.java
 *
 * Created on January 31, 2007, 12:01 PM
 *
 */

package edu.iisc.tdminercore.util;

import edu.iisc.tdminercore.data.EpisodeSet;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.List;

/**
 *
 * @author debprakash
 */
public class EpisodeUitls
{

    private int PARALLEL = 0;
    private int SERIAL = 1;
    
    public static String getMinMaxEpisodeCoverStats(EpisodeSet episodes, List<IEpisode> elist, int index) throws Exception
    {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(b);
        EventFactor eventTypes = episodes.getEventFactor();
        //List<IEpisode> elist = episodes.getEpisodeList(episodes.maxEpisodeSize());
        
        for(int i = 1; i <= episodes.maxEpisodeSize(); i++)
        {
            List<IEpisode> elistSubs = episodes.getEpisodeList(i);

                IEpisode emin = null;
                int fmin = Integer.MAX_VALUE;
                
                IEpisode emax = null;
                int fmax = Integer.MIN_VALUE;
                
                int frac = 0;
                int tot = elistSubs.size();
                for(IEpisode eSub : elistSubs)
                {
                    for(int j = 0; j < elist.size(); j++)
                    {
                        IEpisode e = elist.get(j);
                        if (eSub.isSubEpisode(e) == 1)
                        {
                            frac++;
                            int count = eSub.getVotes(index);
                            if (count > fmax)
                            {
                                fmax = count;
                                emax = eSub;
                            }
                            if (count < fmin)
                            {
                                fmin = count;
                                emin = eSub;
                            }
                            break;
                        }
                    }
                }
                out.print("\t" + emin.getVotes(index));
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(3);
                nf.setMinimumFractionDigits(1);
                out.print("\t" + nf.format((double)frac/(double)tot));
                System.out.println("Min: " + emin.toString(eventTypes) + " : " + emin.getVotes(index));
                System.out.println("Max: " + emax.toString(eventTypes) + " : " + emax.getVotes(index));
                System.out.println("Ratio: " + frac + "/" + tot);
        }
        return b.toString();
    }
}
