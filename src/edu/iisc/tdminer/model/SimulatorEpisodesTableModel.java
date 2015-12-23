/*
 * SimulatorTableModel.java
 *
 * Created on March 30, 2006, 8:17 PM
 *
 */
package edu.iisc.tdminer.model;

import edu.iisc.tdminer.gui.simulator.ISimulatorPanel;
import edu.iisc.tdminercore.data.EventFactor;
import edu.iisc.tdminercore.data.Episode;
import edu.iisc.tdminercore.simulation.ISimulator;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Deb
 */
public class SimulatorEpisodesTableModel extends AbstractTableModel
{

    private static boolean DEBUG = false;
    private ISimulatorPanel component;
    private EventFactor eventTypes;
    private String[] columnNames =
    {
        "Select", "Episodes"
    };
    Class[] types = new Class[]
    {
        Boolean.class, String.class
    };
    public static final int EPISODE_COLUMN = 1;

    /** Creates a new instance of SimulatorTableModel */
    public SimulatorEpisodesTableModel()
    {
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    public int getRowCount()
    {
        if (component != null)
        {
            ISimulator sim = component.getSimulator();
            return sim.getEpisodesList().size();
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
        Object retVal = null;
        if (component != null)
        {
            ISimulator sim = component.getSimulator();
            Episode e = (Episode) sim.getEpisodesList().get(row);

            switch (col)
            {
                case 0:
                    retVal = new Boolean(true);
                    break;
                case 1:
                    retVal = e.toString(eventTypes);
                    break;
            }
        }
        return retVal;
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
            ISimulator sim = component.getSimulator();
            Episode e = (Episode) sim.getEpisodesList().get(row);
            sim.getEpisodesList().remove(row);
        }
        fireTableDataChanged();
    }

    public void setEventTypes(EventFactor eventTypes)
    {
        this.eventTypes = eventTypes;
    }

    public void setSimulatorPanel(ISimulatorPanel component)
    {
        this.component = component;
        fireTableDataChanged();
    }
}

