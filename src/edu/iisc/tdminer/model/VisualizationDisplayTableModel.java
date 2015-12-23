/*
 * VisualizationDisplayTableModel.java
 *
 * Created on March 25, 2006, 2:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package edu.iisc.tdminer.model;

import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.IEpisode;
import edu.iisc.tdminercore.data.EpisodeInstanceSet;
import javax.swing.table.AbstractTableModel;
import ptolemy.plot.Plot;

/**
 *
 * @author Deb
 */
public class VisualizationDisplayTableModel extends AbstractTableModel
{
    private static boolean DEBUG = false;
    private EpisodeInstanceSet data;
    private EventFactor eventTypes;
    private String[] columnNames =
    {
        "Select", "Episodes displayed", "Count"
    };
    private Plot plot;
    Class[] types = new Class[]
    {
        Boolean.class, String.class, String.class
    };
    public static final int EPISODE_COLUMN = 1;
    public static final int FREQUENCY_COLUMN = 2;

    /** Creates a new instance of VisualizationDisplayTableModel */
    public VisualizationDisplayTableModel()
    {
    }

    /** Creates a new instance of VisualizationDisplayTableModel */
    public VisualizationDisplayTableModel(String[] columnNames)
    {
        this.columnNames = columnNames;
    }
    public final Object[] longValues =
    {
        "Frequent Episode discovered", new Integer(100000)
    };

    public int getColumnCount()
    {
        return columnNames.length;
    }

    public int getRowCount()
    {
        if (data != null)
        {
            return data.getEpisodeList().size();
        }
        return 0;
    }

    @Override
    public String getColumnName(int col)
    {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col)
    {
        IEpisode e = data.getEpisodeList().get(row);
        switch (col)
        {
            case 0:
                return new Boolean(true);
            case 1:
                return e.toString(eventTypes);
            case 2:
                return e.getVoteString();
        }
        return null;
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    @Override
    public Class getColumnClass(int columnIndex)
    {
        return types[columnIndex];
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    @Override
    public boolean isCellEditable(int row, int col)
    {
        if (col == 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    @Override
    public void setValueAt(Object value, int row, int col)
    {
        boolean val = ((Boolean) value).booleanValue();
        if (!val)
        {
            IEpisode e = data.getEpisodeList().get(row);
            if (plot != null && e.getIndex() != -1)
            {
                plot.clear(e.getIndex());
            }
            data.removeEpisode(row);
        }
        fireTableDataChanged();
    }

    public EpisodeInstanceSet getData()
    {
        return data;
    }

    public void setData(EpisodeInstanceSet data)
    {
        this.data = data;
        fireTableDataChanged();
    }

    public void setEventTypes(EventFactor eventTypes)
    {
        this.eventTypes = eventTypes;
    }

    public Plot getPlot()
    {
        return plot;
    }

    public void setPlot(Plot plot)
    {
        this.plot = plot;
    }
}
