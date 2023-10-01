package assignment.bluejayAssignment;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Readfile {

	public static void main(String[] args) {
        try {
            // here i give the Excel file path
            String excelFilePath = "C:\\Users\\hp\\Downloads\\Assignment_Timecard.xlsx";
            
            
            
         //read the Excel file
            FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

      // Create a Workbook object to represent the Excel file
            Workbook workbook = new XSSFWorkbook(inputStream);

            Sheet sheet = workbook.getSheetAt(0);

            // map to store employee data 
            Map<String, List<Shift>> employeeShifts = new HashMap<>();

        // check the rows and columns of the sheet
            for (Row row : sheet) {
                // if row and column are empty it will skip using this condition
                if (row.getRowNum() == 0 || row.getCell(0) == null) {
                    continue;
                }
                //here i specify the cell number in given sheet
                String employeeName = row.getCell(7).getStringCellValue(); 
                String position = row.getCell(0).getStringCellValue(); 
                Date timeIn = getDateValue(row.getCell(2)); 
                Date timeOut = getDateValue(row.getCell(3)); 

                if (timeIn == null || timeOut == null) {
                    continue;
                }

                List<Shift> shifts = employeeShifts.getOrDefault(employeeName, new ArrayList<>());

                shifts.add(new Shift(position, timeIn, timeOut));

                employeeShifts.put(employeeName, shifts);
            }

            for (Map.Entry<String, List<Shift>> entry : employeeShifts.entrySet()) {
                String employeeName = entry.getKey();
                List<Shift> shifts = entry.getValue();

                boolean consecutiveDaysWorked = checkConsecutiveDays(shifts);
                boolean lessThan10HoursBetweenShifts = checkHoursBetweenShifts(shifts);
                boolean moreThan14HoursInShift = checkHoursInShift(shifts);

                if (consecutiveDaysWorked || lessThan10HoursBetweenShifts || moreThan14HoursInShift) {
                    System.out.println(employeeName + " (" + shifts.get(0).getPosition() + ")");
                }
            }

            workbook.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Date getDateValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
            return cell.getDateCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                return dateFormat.parse(cell.getStringCellValue());
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    // method to check employee has worked for 7 consecutive days
    private static boolean checkConsecutiveDays(List<Shift> shifts) {
        if (shifts.size() < 7) {
            return false;
        }

        Collections.sort(shifts, Comparator.comparing(Shift::getStartTime));

        for (int i = 0; i < shifts.size() - 6; i++) {
            long daysBetween = daysBetween(shifts.get(i).getEndTime(), shifts.get(i + 6).getStartTime());
            if (daysBetween <= 7) {
                return true;
            }
        }

        return false;
    }

    //method for calculating number of days between two dates
    private static long daysBetween(Date startDate, Date endDate) {
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();
        long diffTime = endTime - startTime;
        return diffTime / (24 * 60 * 60 * 1000);
    }

    //method check if employee has worked less than 10 hours between shifts but greater than 1 hour
    private static boolean checkHoursBetweenShifts(List<Shift> shifts) {
        Collections.sort(shifts, Comparator.comparing(Shift::getStartTime));

        for (int i = 1; i < shifts.size(); i++) {
            long hoursBetween = hoursBetween(shifts.get(i - 1).getEndTime(), shifts.get(i).getStartTime());
            if (hoursBetween < 10 && hoursBetween > 1) {
                return true;
            }
        }

        return false;
    }

    //method for calculating  the number of hours between two dates
    private static long hoursBetween(Date startDate, Date endDate) {
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();
        long diffTime = endTime - startTime;
        return diffTime / (60 * 60 * 1000);
    }

    // method check employee has worked for more than 14 hours in a single shift
    private static boolean checkHoursInShift(List<Shift> shifts) {
        for (Shift shift : shifts) {
            long hoursWorked = hoursBetween(shift.getStartTime(), shift.getEndTime());
            if (hoursWorked > 14) {
                return true;
            }
        }

        return false;
    }

    static class Shift {
        private String position;
        private Date startTime;
        private Date endTime;

        public Shift(String position, Date startTime, Date endTime) {
            this.position = position;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getPosition() {
            return position;
        }

        public Date getStartTime() {
            return startTime;
        }

        public Date getEndTime() {
            return endTime;
        }
    }


}
