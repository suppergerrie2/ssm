/**
 * Simple Stack Machine
 *
 * Written by Atze Dijkstra, atze@cs.uu.nl,
 * Copyright Utrecht University.
 *
 */

package nl.uu.cs.ssm ;

import java.awt.Color;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Machine
{
    protected Memory            memory      ;
    protected Registers         registers   ;
    protected MachineState      state       ;
    
    private Messenger           messenger   ;
    
    public Machine( MachineState st, Messenger m )
    {
        state = st ;
        messenger = m ;
        reset() ;
    }
    
    public void reset()
    {
        state.reset() ;
        memory = state.getMemory() ;
        registers = state.getRegisters( ) ;
    }
    
    public MachineState state()
    {
    	return state ;
    }
    
    public Memory memory()
    {
    	return memory ;
    }
    
    public Registers registers()
    {
    	return registers ;
    }
    
    private int dir( int v )
    {
    	return state.dir( v ) ;
    }
    
    private void annote( MemoryAnnotation ann )
    {
        memory.setAnnotationAt( registers.getReg( Registers.SP ), ann ) ;
    }
    
    private void copyMem( int fromA, int toA, int size, MemoryAnnotation ann )
    {
    	if ( dir(fromA) < dir(toA) )
    	{
    		for ( int i = size-1 ; i >= 0 ; i-- )
	    		memory.setAt( toA + dir(i), memory.getAt( fromA + dir(i) ) ) ;
    	}
    	else
    	{
	    	for ( int i = 0 ; i < size ; i++ )
	    		memory.setAt( toA + dir(i), memory.getAt( fromA + dir(i) ) ) ;
    	}
    	for ( int i = 0 ; i < size ; i++ )
    		memory.setAnnotationAt( toA + dir(i), ann ) ;
    }
    
    private void pushMultiple( int fromA, int sz, MemoryAnnotation ann )
    {
    	int toA = registers.getReg( Registers.SP ) + dir(1) ;
        registers.adjustSP( dir(sz) ) ;
        copyMem( fromA, toA, sz, ann ) ;
    }
    
    private void pushMultiple( int fromA, int sz, Instruction instr )
    {
        pushMultiple( fromA, sz, new MemoryAnnotation( instr.getRepr(), null ) ) ;
    }
    
    private void pushMultiple( int fromA, int sz )
    {
        pushMultiple( fromA, sz, state.instr ) ;
    }
    
    private void push( int v, MemoryAnnotation ann )
    {
        registers.adjustSP( dir(1) ) ;
        registers.setRegInd( Registers.SP, v ) ;
        annote( ann ) ;
    }
    
    private void push( int v, Instruction instr )
    {
        push( v, new MemoryAnnotation( instr.getRepr(), null ) ) ;
    }
    
    private void push( int v )
    {
        push( v, state.instr ) ;
    }
    
    private void pushCopyOfReg( int r )
    {
        push( registers.getReg(r), new MemoryAnnotation( "copy of " + Registers.getRegOrAliasName(r), Color.cyan ) ) ;
    }
    
    private void pushPCAsReturnAddr()
    {
        push( registers.getReg(Registers.PC), new MemoryAnnotation( "return addr ", Color.red ) ) ;
    }
    
    private void pushMP()
    {
        push( registers.getReg(Registers.MP), new MemoryAnnotation( "prev " + Registers.getRegOrAliasName(Registers.MP), Color.blue ) ) ;
    }
    
    private int pop()
    {
        annote( null ) ;
        int v = registers.getRegInd( Registers.SP ) ;
        registers.adjustSP( dir(-1) ) ;
        return v ;
    }
    
    private void popMultiple( int toA, int sz )
    {
        registers.adjustSP( dir(-sz) ) ;
        int fromA = registers.getReg( Registers.SP ) + dir(1) ;
        copyMem( fromA, toA, sz, null ) ;
    }
    
    public MachineState getMachineState()
    {
        return state ;
    }
    
    private int fetchNextInstr()
    {
        int pc = registers.getPC() ;
        int oldpc = pc ;
        int code = memory.getAt( pc ) ;
        pc++ ;
        Instruction instr = Instruction.findByCode( code ) ;
        if ( instr == null )
        {
            messenger.println( "illegal instruction code " + Utils.asHex(code) ) ;
            code = Instruction.I_HALT ;
            instr = Instruction.findByCode( code ) ;
        }
        else
        {
            state.setCurrentInstr( oldpc, code, instr ) ;
            for ( int i = 0 ; i < state.nInlineOpnds ; i++ )
            {
                state.inlineOpnds[ i ] = memory.getAt( pc++ ) ;
            }
        }
        registers.setPC( pc ) ;
        return code ;
    }
    
    public int execBinop( int code, int o1, int o2 )
    {
        switch( code )
        {
            case Instruction.BI_ADD :
                o1 += o2 ;
                break ;
                
            case Instruction.BI_SUB :
                o1 -= o2 ;
                break ;
                
            case Instruction.BI_MUL :
                o1 *= o2 ;
                break ;
                
            case Instruction.BI_DIV :
                o1 /= o2 ;
                break ;
                
            case Instruction.BI_MOD :
                o1 %= o2 ;
                break ;
                
            case Instruction.BI_AND :
                o1 &= o2 ;
                break ;
                
            case Instruction.BI_OR :
                o1 |= o2 ;
                break ;
                
            case Instruction.BI_XOR :
                o1 ^= o2 ;
                break ;
                
            case Instruction.BI_LSL :
                o1 <<= o2 ;
                break ;
                
            case Instruction.BI_LSR :
                o1 >>= o2 ;
                break ;
                
            case Instruction.BI_ROL :
                o1 = ( o1 << o2 ) | ( o1 >> ( Instruction.nWordBits - o2 ) ) ;
                break ;
                
            case Instruction.BI_ROR :
                o1 = ( o1 >> o2 ) | ( o1 << ( Instruction.nWordBits - o2 ) ) ;
                break ;
                
            case Instruction.BI_EQ :
                o1 = o1 == o2 ? Instruction.CONST_TRUE : Instruction.CONST_FALSE ;
                break ;
                
            case Instruction.BI_NE :
                o1 = o1 != o2 ? Instruction.CONST_TRUE : Instruction.CONST_FALSE ;
                break ;
                
            case Instruction.BI_LT :
                o1 = o1 < o2 ? Instruction.CONST_TRUE : Instruction.CONST_FALSE ;
                break ;
                
            case Instruction.BI_GT :
                o1 = o1 > o2 ? Instruction.CONST_TRUE : Instruction.CONST_FALSE ;
                break ;
                
            case Instruction.BI_LE :
                o1 = o1 <= o2 ? Instruction.CONST_TRUE : Instruction.CONST_FALSE ;
                break ;
                
            case Instruction.BI_GE :
                o1 = o1 >= o2 ? Instruction.CONST_TRUE : Instruction.CONST_FALSE ;
                break ;
                
            /*
            case Instruction.BI_CMP :
                int sr, r, c1, c2 ;
                sr = c1 = c2 = 0 ;
                r = o1 - o2 ;
                if ( r == 0 )
                      sr |= Instruction.ZZ ;
                if ( (int)r < 0 )
                      sr |= Instruction.NN ;
                r = ( o1 >> 1 ) + ( ( - o2 ) >> 1 ) ;
                if ( (int)r < 0 )
                {
                      sr |= Instruction.CC ;
                      c1 = Instruction.VV ;
                }
                r = ( ( o1 << 1 ) >> 1 ) + ( ( ( - o2 ) << 1 ) >> 1 ) ;
                if ( (int)r < 0 )
                      c2 = Instruction.VV ;
                sr |= ( c1 ^ c2 ) & Instruction.VV ;
                o1 = sr ;
                break ;
            */
                
        }
        return o1 ;
    }
    
    public int execUnop( int code, int o1 )
    {
        switch( code )
        {
            case Instruction.UI_NEG :
                o1 = -o1 ;
                break ;
                
            case Instruction.UI_NOT :
                o1 = ~o1 ;
                break ;
                
        }
        return o1 ;
    }
    
    public void halt()
    {
        state.setHalted() ;
        messenger.println( "machine halted" ) ;
    }
    
    public void executeOne()
    {
        if ( state.isHalted )
            return ;

        int code = fetchNextInstr() ;
        //System.out.println( "exec1 " + state ) ;
        int tmp1, tmp2, tmp3, addr, offset, size ;
        
        switch( state.instr.getCategory() )
        {
            case Instruction.CTG_BINOP :
                tmp2 = pop() ;
                tmp1 = pop() ;
                push( execBinop( code, tmp1, tmp2 ) ) ;
                annote(new MemoryAnnotation(String.format("%1$d %2$s %3$d", tmp1, state.instr.getRepr(), tmp2), null));
                break ;
                
            case Instruction.CTG_UNOP :
                tmp1 = pop() ;
                push( execUnop( code, tmp1 ) ) ;
                break ;
                
            case Instruction.CTG_OP :
                switch( code )
                {
                    case Instruction.I_ADJS :
                        registers.adjustReg( Registers.SP, dir(state.inlineOpnds[ 0 ]) ) ;
                        break ;
                        
                    case Instruction.I_BRA :
                        registers.adjustReg( Registers.PC, state.inlineOpnds[ 0 ] ) ;
                        break ;
                        
                    case Instruction.I_BRF :
                    	tmp1 = pop() ;
                    	if ( tmp1 == 0 )
	                        registers.adjustReg( Registers.PC, state.inlineOpnds[ 0 ] ) ;
                        break ;
                        
                    case Instruction.I_BRT :
                    	tmp1 = pop() ;
                    	if ( tmp1 != 0 )
	                        registers.adjustReg( Registers.PC, state.inlineOpnds[ 0 ] ) ;
                        break ;
                        
                    case Instruction.I_BSR :
                        pushPCAsReturnAddr() ;
                        registers.adjustReg( Registers.PC, state.inlineOpnds[ 0 ] ) ;
                        break ;
                        
                    case Instruction.I_HALT :
                        halt() ;
                        break ;
                        
                    case Instruction.I_JSR :
                        tmp1 = pop() ;
                        pushPCAsReturnAddr() ;
                        registers.setReg( Registers.PC, tmp1 ) ;
                        break ;
                        
                    case Instruction.I_LDS :
                        push( registers.getRegDisplInd( Registers.SP, dir(state.inlineOpnds[ 0 ]) ) ) ;
                        break ;
                        
                    case Instruction.I_LDMS :
                        pushMultiple( registers.getRegDispl( Registers.SP, dir(state.inlineOpnds[ 0 ]) ), state.inlineOpnds[ 1 ] ) ;
                        break ;
                        
                    case Instruction.I_LDA :
                        push( memory.getAt( pop() + dir(state.inlineOpnds[ 0 ]) ) ) ;
                        break ;
                        
                    case Instruction.I_LDMA :
                        pushMultiple( pop() + dir(state.inlineOpnds[ 0 ]), state.inlineOpnds[ 1 ] ) ;
                        break ;
                        
                    case Instruction.I_LDC :
                        push( state.inlineOpnds[ 0 ] ) ;
                        break ;
                        
                    case Instruction.I_LDL :
                        push( registers.getRegDisplInd( Registers.MP, dir(state.inlineOpnds[ 0 ]) ) ) ;
                        break ;
                        
                    case Instruction.I_LDML :
                        pushMultiple( registers.getRegDispl( Registers.MP, dir(state.inlineOpnds[ 0 ]) ), state.inlineOpnds[ 1 ] ) ;
                        break ;
                        
                    case Instruction.I_LDAA :
                        push( pop() + state.inlineOpnds[ 0 ] ) ;
                        break ;
                        
                    case Instruction.I_LDSA :
                        push( registers.getRegDispl( Registers.SP, dir(state.inlineOpnds[ 0 ]) ) ) ;
                        break ;
                        
                    case Instruction.I_LDLA :
                        push( registers.getRegDispl( Registers.MP, dir(state.inlineOpnds[ 0 ]) ) ) ;
                        break ;
                        
                    case Instruction.I_LDR :
                        pushCopyOfReg( state.inlineOpnds[ 0 ] ) ;
                        break ;
                        
                    case Instruction.I_LDRR :
                        registers.setReg( state.inlineOpnds[ 0 ], registers.getReg( state.inlineOpnds[ 1 ] ) ) ;
                        break ;
                        
                    case Instruction.I_LINK :
                    	pushMP( ) ;
                    	registers.setReg( Registers.MP, registers.getReg( Registers.SP ) ) ;
                    	tmp1 = registers.getReg( Registers.SP ) + dir(1) ;
                        registers.adjustReg( Registers.SP, dir(state.inlineOpnds[ 0 ]) ) ;
                    	tmp2 = registers.getReg( Registers.SP ) ;
                        break ;
                        
                    case Instruction.I_NOP :
                    	break ;
                        
                    case Instruction.I_RET :
                    	registers.setReg( Registers.PC, pop() ) ;
                    	break ;
                        
                    case Instruction.I_STS :
                    	tmp1 = registers.getRegDispl( Registers.SP, dir(state.inlineOpnds[ 0 ]) ) ;
                        memory.setAt( tmp1, pop() ) ;
                        break ;
                        
                    case Instruction.I_STMS :
                    	popMultiple( registers.getRegDispl( Registers.SP, dir(state.inlineOpnds[ 0 ]) ), state.inlineOpnds[ 1 ] ) ;
                        break ;
                        
                    case Instruction.I_STA :
                    	tmp1 = pop() ;
                    	tmp2 = pop() ;
                        memory.setAt( tmp1 + state.inlineOpnds[ 0 ], tmp2 ) ;
                        break ;
                        
                    case Instruction.I_STMA :
                    	tmp1 = pop() ;
                    	popMultiple( tmp1 + state.inlineOpnds[ 0 ], state.inlineOpnds[ 1 ] ) ;
                        break ;
                        
                    case Instruction.I_STL :
                    	registers.setRegDisplInd( Registers.MP, dir(state.inlineOpnds[ 0 ]), pop() ) ;
                        break ;
                        
                    case Instruction.I_STML :
                    	popMultiple( registers.getRegDispl( Registers.MP, dir(state.inlineOpnds[ 0 ]) ), state.inlineOpnds[ 1 ] ) ;
                        break ;
                        
                    case Instruction.I_STR :
                        registers.setReg( state.inlineOpnds[ 0 ], pop() ) ;
                        break ;
                        
                    case Instruction.I_SWP :
                    	tmp1 = registers.getRegDisplInd( Registers.SP, dir(-1) ) ;
                        registers.setRegDisplInd( Registers.SP, dir(-1), registers.getRegInd( Registers.SP ) ) ;
                        registers.setRegInd( Registers.SP, tmp1 ) ;
                        break ;
                        
                    case Instruction.I_SWPR :
                        tmp1 = registers.getRegInd( Registers.SP ) ;
                        tmp2 = state.inlineOpnds[ 0 ] ;
                        registers.setRegInd( Registers.SP, registers.getReg( tmp2 ) ) ;
                        registers.setReg( tmp2, tmp1 ) ;
                        break ;
                        
                    case Instruction.I_SWPRR :
                        tmp1 = state.inlineOpnds[ 0 ] ;
                        tmp2 = state.inlineOpnds[ 1 ] ;
                        tmp3 = registers.getReg( tmp1 ) ;
                        registers.setReg( tmp1, registers.getReg( tmp2 ) ) ;
                        registers.setReg( tmp2, tmp3 ) ;
                        break ;
                        
                    case Instruction.I_UNLINK :
                    	registers.setReg( Registers.SP, registers.getReg( Registers.MP ) ) ;
                    	registers.setReg( Registers.MP, pop() ) ;
                        break ;
                        
                    case Instruction.I_TRAP :
                        switch( state.inlineOpnds[ 0 ] )
                        {
                            case Instruction.TR_PR_INT :
                                messenger.println( "" + pop() ) ;
                                break ;
                            case Instruction.TR_PR_CHAR :
                                try
                                {
                                    messenger.print( "" + Utils.codePointToString(pop()) ) ;
                                }
                                catch (UnsupportedEncodingException e)
                                {
                                    messenger.println("Error: UTF-32 encoding missing.");
                                }
                                break;
                            case Instruction.TR_IN_INT :
                                push(messenger.promptInt());
                                break;
                            case Instruction.TR_IN_CHAR :
                                push(messenger.promptChar());
                                break;
                            case Instruction.TR_IN_CHAR_ARRAY :
                                push(0);
                                int[] chars = messenger.promptCharArray();
                                for (int i = chars.length - 1; i >= 0; i--)
                                {
                                    push(chars[i]);
                                }
                                break;
                            case Instruction.TR_FILE_OPEN_READ :
                            case Instruction.TR_FILE_OPEN_WRITE :
                                StringBuilder filename = new StringBuilder();
                                int n = pop();
                                while (n != 0)
                                {
                                    filename.append((char)n);
                                    n = pop();
                                }
                                String fname = filename.toString();
                                try
                                {
                                    boolean readOnly = state.inlineOpnds[0] == Instruction.TR_FILE_OPEN_READ;
                                    push(state.openFile(fname, readOnly));
                                }
                                catch (IOException e)
                                {
                                    messenger.println("Error: file "+fname+" not found");
                                }
                                break;
                            case Instruction.TR_FILE_READ :
                                try
                                {
                                    push(state.readFromFile(pop()));
                                }
                                catch (IOException e)
                                {
                                    messenger.println("Error: cannot read from file.");
                                }
                                catch (IndexOutOfBoundsException e)
                                {
                                    messenger.println("Error: invalid file pointer.");
                                }
                                catch (NullPointerException e)
                                {
                                    messenger.println("Error: invalid file pointer.");
                                }
                                break;
                            case Instruction.TR_FILE_WRITE :
                                try
                                {
                                    push(state.writeToFile(pop(), pop()));
                                }
                                catch (IOException e)
                                {
                                    messenger.println("Error: cannot write to file.");
                                }
                                catch (IndexOutOfBoundsException e)
                                {
                                    messenger.println("Error: invalid file pointer.");
                                }
                                catch (NullPointerException e)
                                {
                                    messenger.println("Error: invalid file pointer.");
                                }
                                break;
                            case Instruction.TR_FILE_CLOSE :
                                try
                                {
                                    state.closeFile(pop());
                                }
                                catch (IOException e)
                                {
                                    messenger.println("Error: cannot close file.");
                                }
                                catch (IndexOutOfBoundsException e)
                                {
                                    messenger.println("Error: invalid file pointer.");
                                }
                                catch (NullPointerException e)
                                {
                                    messenger.println("Error: invalid file pointer.");
                                }
                                break;
                            default : break ;
                        }
                        break ;
                        
                    case Instruction.I_LDH :
                    	// This is exactly the same as LDA but left here for symmetry with the other heap instructions
                    	addr = pop();
                    	offset = state.inlineOpnds[0];
                    	push(memory.getAt(addr + offset));
                    	break;
                    case Instruction.I_LDMH :
                    	addr = pop();
                    	offset = state.inlineOpnds[0];
                    	size = state.inlineOpnds[1];
                    	pushMultiple(addr - offset - (size - 1), size);
                    	break;
                        
                    case Instruction.I_STH :      
                    	tmp1 = pop();
                    	addr = registers.getHP();
                    	registers.adjustHP(1);
                    	memory.setAt(addr, tmp1); 
                    	memory.setAnnotationAt(addr, new MemoryAnnotation("begin / end", null));
                    	push(addr);
                    	break;
                    	
                    case Instruction.I_STMH :
                    	size = state.inlineOpnds[0];
                    	int beginAddr = registers.getHP();
                    	int endAddr = beginAddr + size - 1;
                    	registers.adjustHP(size);
                    	popMultiple(beginAddr, size);
                    	if(size == 1) {
                    		memory.setAnnotationAt(beginAddr, new MemoryAnnotation("begin / end", null));
                    	} else {
                    		memory.setAnnotationAt(beginAddr, new MemoryAnnotation("begin", null));
                        	memory.setAnnotationAt(endAddr, new MemoryAnnotation("end", null));	
                    	}
                    	push(endAddr);
                    	break;
                        
                    default :
                        messenger.println( state.instr + " at " + Utils.asHex(state.instrPC) + " not (yet) implemented" ) ;
                        break ;
                }
                break ;
            
            case Instruction.CTG_BRCC :
                if ( state.instr.srMatchesCCInfo( pop() ) )
                    registers.adjustReg( Registers.PC, state.inlineOpnds[ 0 ] ) ;                        
                break ;
                
            default :
                messenger.println( state.instr + " at " + Utils.asHex(state.instrPC) + " not (yet) implemented" ) ;
                break ;
                
        }
        
    }
    
}