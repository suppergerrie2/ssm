/* 
	Runner.java

	Title:			Simple Stack Machine Runner
	Author:			atze
	Description:	
*/

package nl.uu.cs.ssmui;

import java.io.File;

import javax.swing.UIManager;

public class Runner extends Thread
{
    protected int delay = 50 ;
    
    SSMRunner  ssmRunner  ;
    
	public Runner( File initialFile ) 
	{
		try {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} 
			catch (Exception e) { 
			}
		    ssmRunner = new SSMRunner( this );
			ssmRunner.initComponents();
			ssmRunner.setVisible(true);
			//System.out.println( "Foc Trav=" + ssmRunner.isFocusTraversable() ) ;
			ssmRunner.requestFocus() ;
			if ( initialFile != null )
				ssmRunner.loadFile( initialFile ) ;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		while( true )
		{
			int steppingState = ssmRunner.steppingState() ;
			if ( steppingState != SSMRunner.STEP_BY_STEP )
			{
				if ( ssmRunner.hasBreakpointAtPC() )
					ssmRunner.stopContinuouslyDoingSteps() ;
				else if ( steppingState == SSMRunner.STEP_CONT_FORWARD )
					ssmRunner.doAStepForward() ;
				else if ( steppingState == SSMRunner.STEP_CONT_BACKWARD )
					ssmRunner.doAStepBack() ;
			}
			try { sleep( delay ) ; } catch ( InterruptedException e ) {}
		}
	}

	// Main entry point
	static public void main(String[] args) 
	{
		File initialFile = null ;
		if ( args.length > 0 )
		{
			File f = new File( args[0] ) ;
			if ( f.exists() )
				initialFile = f ;
		}
		new Runner( initialFile ) ;
	}
	
}
