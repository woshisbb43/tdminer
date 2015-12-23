package edu.iisc.tdminercore.simulation;

/**
 * Class: ExternalInput
 *
 * @author debprakash
 */
public class ExternalInput {
    private int neuron;
    private String file;
    private boolean selected = true;

    public ExternalInput(int neuron, String file)
    {
        this.neuron = neuron;
        this.file = file;
    }
    public int getNeuron()
    {
        return neuron;
    }

    public String getFile()
    {
        return file;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 89 * hash + this.neuron;
        hash = 89 * hash + (this.file != null ? this.file.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final ExternalInput other = (ExternalInput) obj;
        if (this.neuron != other.neuron)
        {
            return false;
        }
        return true;
    }
}
