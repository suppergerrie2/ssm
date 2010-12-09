package nl.uu.cs.ssmui;import javax.swing.event.TableModelEvent;import javax.swing.table.AbstractTableModel;import nl.uu.cs.ssm.ColoredText;import nl.uu.cs.ssm.MachineState;import nl.uu.cs.ssm.Memory;import nl.uu.cs.ssm.MemoryAnnotation;import nl.uu.cs.ssm.MemoryCellEvent;import nl.uu.cs.ssm.MemoryCellListener;import nl.uu.cs.ssm.Registers;import nl.uu.cs.ssm.Utils;public class StackTableModel extends AbstractTableModel    implements MemoryCellListener{	private static final long serialVersionUID = 1L ;    private static final int C_ADDRESS = 0 ;    private static final int C_VALUE   = 1 ;    protected static final int C_REGPTRS = 2 ;    public  static final int C_ANNOTE  = 3 ;        private static final String[] columnNames = { "Address", "Value", "RegPtrs", "Annote" } ;        private MachineState		machineState ;    private Memory 				memory ;    private Registers 			registers ;        private int                 beforeMinAccessedSP ;    private int                 maxAccessedSP ;        private boolean				isSPChanged = false ;    public void beforeReset()    {    }        public void reset()    {        if ( memory != null )        	memory.removeMemoryCellListener( this ) ;        if ( registers != null )	        registers.removeMemoryCellListener( this ) ;        memory = machineState.getMemory() ;        registers = machineState.getRegisters() ;        memory.addMemoryCellListener( this ) ;        registers.addMemoryCellListener( this ) ;        beforeMinAccessedSP = maxAccessedSP = registers.getReg( registers.SP ) ;        fireTableChanged( new TableModelEvent( this ) ) ;        //fireTableStructureChanged() ;    }        public StackTableModel( MachineState mst )    {    	machineState = mst ;    	reset() ;    }        protected boolean isSPChanged()    {    	return isSPChanged ;    }    public int getColumnCount()    {        return columnNames.length ;    }    public int getRowCount()    {        return machineState.dir( maxAccessedSP - beforeMinAccessedSP ) ;        //return memory.getCapacity() ;    }    private int rowToMemLoc( int row )    {        return row + (beforeMinAccessedSP + 1) ;        //return getRowCount() - row - 1 ;    }        protected int memLocToRow( int loc )    {        return loc - (beforeMinAccessedSP + 1) ;        //return rowToMemLoc( loc ) ;    }        public boolean isCellEditable( int row, int column )    {        return column == C_VALUE ;    }        private boolean isMemLocWithinStackRange( int memLoc )    {        return memLoc <= maxAccessedSP && memLoc > beforeMinAccessedSP ;    }        public Object getValueAt( int row, int column )    {        Object res = "" ;        int memLoc = rowToMemLoc( row ) ;        switch( column )        {            case C_ADDRESS :                res = Utils.asHex( memLoc ) ;                break ;                            case C_VALUE :                res = Utils.asHex( memory.getAt( memLoc ) ) ;                break ;                            case C_REGPTRS :                {                    String pc, sp, mp ;                    pc = sp = mp = "" ;                    if ( memLoc == registers.getReg( registers.PC ) )                        pc = " PC" ;                    if ( memLoc == registers.getReg( registers.SP ) )                        sp = " SP" ;                    if ( memLoc == registers.getReg( registers.MP ) )                        mp = " MP" ;                    res = pc + sp + mp ;                }                break ;            case C_ANNOTE :                MemoryAnnotation ann = memory.getAnnotationAt( memLoc ) ;                //System.out.println( "annote stack at " + memLoc + "=" + ann ) ;                res = ann == null ? ColoredText.blankDefault : ann ;                break ;                        }        return res ;    }    public void setValueAt( Object aValue, int row, int column )    {        if ( column == C_VALUE )        {            String strValue = null ;            if ( aValue instanceof String )                strValue = (String)aValue ;            else                strValue = aValue.toString() ;            memory.setAt( rowToMemLoc( row ), strValue ) ;        }    }        public String getColumnName( int column )    {        return columnNames[ column ] ;    }    public Class getColumnClass( int column )    {    	if ( column == C_ANNOTE )	    	return ColoredText.class ;    	else	        return SSMRunner.tableModelColumnClass ;    }    public void cellChanged( MemoryCellEvent e )    {    	Object src = e.getSource() ;    	int v ;    	if ( src == memory )    	{    	    int row = memLocToRow( e.cellIndex ) ;    	    if ( isMemLocWithinStackRange( e.cellIndex ) )        		fireTableRowsUpdated( row, row ) ;    	}    	else if ( src == registers && e.event == e.CELL )    	{    	    int loc = registers.getReg( e.cellIndex ) ;    	    if ( e.cellIndex == registers.SP && maxAccessedSP < loc )    	    {    	        int oldSPRow = rowToMemLoc( maxAccessedSP ) ;    	        int newSPRow = rowToMemLoc( loc ) ;    	        maxAccessedSP = loc ;    	        isSPChanged = true ;                fireTableRowsInserted( oldSPRow+1, newSPRow ) ;    	    }    	    else    	    	isSPChanged = false ;    	    	    	    if ( isMemLocWithinStackRange( v = e.getOldCellValue() ) )    		{        	    int oldrow = memLocToRow( v ) ;    		    fireTableCellUpdated( oldrow, C_REGPTRS ) ;    		}    	    if ( isMemLocWithinStackRange( loc ) )    		{        	    int row = memLocToRow( loc ) ;    		    fireTableCellUpdated( row, C_REGPTRS ) ;    		}    	}    }}