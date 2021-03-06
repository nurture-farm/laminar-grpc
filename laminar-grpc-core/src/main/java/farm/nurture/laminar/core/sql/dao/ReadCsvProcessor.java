/*
 *  Copyright 2022 Nurture.Farm
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package farm.nurture.laminar.core.sql.dao;

public class ReadCsvProcessor {
	
	public static final int INITIAL_STRING_SIZE = 128;	
	private char separator = ',';
	private char escapechar;
	private String lineEnd = "\n";
    private char quotechar;
    /** The quote constant to use when you wish to suppress all quoting. */
    public static final char NO_QUOTE_CHARACTER = '\u0000';
    
    /** The escape constant to use when you wish to suppress all escaping. */
    public static final char NO_ESCAPE_CHARACTER = '\u0000';

    public ReadCsvProcessor(char separator) {
        this.separator = separator;
        this.quotechar = NO_QUOTE_CHARACTER;
        this.escapechar = NO_ESCAPE_CHARACTER;
    }

    public ReadCsvProcessor(char separator, char quotechar, char escapechar) {
        this.separator = separator;
        this.quotechar = quotechar;
        this.escapechar = escapechar;
    }    
	
    /**
     * Write a row.
     * @param cells
     * @param fillBuffer
     * @return
     */
    public void writeRow(String[] cells, StringBuilder fillBuffer) {
        	
        	if (cells == null) return;
        	
        	fillBuffer.delete(0, fillBuffer.capacity());
            for (int i = 0; i < cells.length; i++) {

                if (i != 0) {
                    fillBuffer.append(separator);
                }

                String nextElement = cells[i];
                if (nextElement == null)
                    continue;
                if (quotechar !=  NO_QUOTE_CHARACTER)
                	fillBuffer.append(quotechar);
                
                fillBuffer.append(hasSpecialCharacters(nextElement) ? escapeSpecialCharacters(nextElement) : nextElement);

                if (quotechar != NO_QUOTE_CHARACTER)
                	fillBuffer.append(quotechar);
            }
            
            fillBuffer.append(lineEnd);
        }    
    
	private boolean hasSpecialCharacters(String line) {
	    return line.indexOf(quotechar) != -1 || line.indexOf(escapechar) != -1;
    }
	
	private StringBuilder escapeSpecialCharacters(String nextElement)
    {
		StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
	    for (int j = 0; j < nextElement.length(); j++) {
	        char nextChar = nextElement.charAt(j);
	        if (escapechar != NO_ESCAPE_CHARACTER && nextChar == quotechar) {
	        	sb.append(escapechar).append(nextChar);
	        } else if (escapechar != NO_ESCAPE_CHARACTER && nextChar == escapechar) {
	        	sb.append(escapechar).append(nextChar);
	        } else {
	            sb.append(nextChar);
	        }
	    }
	    return sb;
    }	
}
