package org.arrah.framework.xls;

/***********************************************
 *     Copyright to vivek Kumar Singh          *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This files is used for reading,writing xlsx files
 *
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;


import org.apache.poi.ss.util.CellRangeAddress;
import org.arrah.framework.ndtable.ReportTableModel;

public class XlsxReader {
	private ReportTableModel _rt = null;
	private ArrayList<String> columnName = new ArrayList<String> ();
	private boolean allSheet = false;

	public XlsxReader() {
	};
	
	public void readAllSheets(boolean readAll) {
		allSheet = readAll;
	}

	public ReportTableModel read(File file) {
		if (loadXlsxFile(file) == true)
			return _rt;
		else {
			System.out.println("XLSX File can not be loaded");
			return null;
		}
	}

	// Send Double, Boolean LocalDate not Date
		private  boolean loadXlsxFile(File fileName) {
		int colI=0; // column Index
		boolean headerSet= false; // for multiple sheets
		
		try {

			Workbook workbook = WorkbookFactory.create(fileName);
			int noOfSheet = workbook.getNumberOfSheets();
			System.out.println("No of Sheets in Xlsx:"+noOfSheet);
			
			// It takes all the sheets
			java.util.Iterator<Sheet> shiter = workbook.sheetIterator();
			
			while (shiter.hasNext()) {
				Sheet sheet = shiter.next();
			
			// Fill the mergered Cell value
			Hashtable<String,String> mergeV = fillMergedVal( sheet);
			
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				Iterator<Cell> cellIterator = row.cellIterator();
				if (row.getRowNum() == 0) { // this is header - default behaviour
					if (headerSet == true)
						continue;
					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();
						columnName.add(cell.getStringCellValue());
					}
					_rt = new ReportTableModel(columnName.toArray(), true, true);
					headerSet = true;
					continue;
				} else {
					colI =0; // Start of new Row
					ArrayList<Object> rowmap = new ArrayList<Object>(); // to hold Row
					while (colI < columnName.size()) {
						Cell cell = row.getCell(colI);
						if (cell == null) {
							String checkM = mergeV.get(row.getRowNum()+":"+colI);
							if (checkM == null)
								rowmap.add("");
							else
								rowmap.add(checkM);
							colI++;
							continue;
						}
						CellType ct = cell.getCellTypeEnum();
						
						if (ct == CellType.STRING) {
							rowmap.add(cell.getStringCellValue());
						} else if (ct == CellType.NUMERIC) {
							if (DateUtil.isCellDateFormatted(cell)) {
								long epochtime = cell.getDateCellValue().getTime(); // in milli second
								rowmap.add(new Date(epochtime));
							} else {
								rowmap.add(cell.getNumericCellValue());
							}
						} else if (ct == CellType.BLANK || ct == CellType.ERROR) {
							String checkM = mergeV.get(row.getRowNum()+":"+colI);
							if (checkM == null)
								rowmap.add("");
							else
								rowmap.add(checkM);
						} else if (ct == CellType.BOOLEAN) {
							rowmap.add(cell.getBooleanCellValue());
						} else { // default Behavior
							rowmap.add(cell.toString());
						}
						colI++; // Move to next column
					}
					_rt.addFillRow(rowmap.toArray());
				}
			} // End of while loop
			if (allSheet == false)
				break; // no need for next iteration
		} // end of iterator
		} catch ( FileNotFoundException e) {
			System.out.println("File Not found");
			return false;
		} catch (Exception e) {
			System.out.println("Exception:"+e.getClass().getSimpleName()+ " Message: "+e.getLocalizedMessage());
			return false;
		}
		return true;
	}
		
	private Hashtable<String,String> fillMergedVal(Sheet sheet) {
		Hashtable<String,String> mergerVal = new Hashtable<String,String>();
		//will iterate over the Merged cells
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i); //Region of merged cells

            int colIndex = region.getFirstColumn(); //number of columns merged
            int rowNum = region.getFirstRow();      //number of rows merged
            Cell cell = sheet.getRow(rowNum).getCell(colIndex);
            String cellV= "";
            if (cell == null)
            	mergerVal.put(rowNum+":"+colIndex, cellV);
            else
            	mergerVal.put(rowNum+":"+colIndex, cellV = cell.getStringCellValue());
            
            //fill merged value for region
            for (rowNum = region.getFirstRow(); rowNum <= region.getLastRow(); rowNum++)
            	for (colIndex = region.getFirstColumn(); colIndex <= region.getLastColumn(); colIndex++)
            		mergerVal.put(rowNum+":"+colIndex, cellV);
            
        }
		return mergerVal;
		
	}
}