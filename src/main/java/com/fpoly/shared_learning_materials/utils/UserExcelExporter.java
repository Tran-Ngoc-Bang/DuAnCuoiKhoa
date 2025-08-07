package com.fpoly.shared_learning_materials.utils;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fpoly.shared_learning_materials.domain.User;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

public class UserExcelExporter {
	private XSSFWorkbook workbook;
	private Sheet sheet;

	public UserExcelExporter() {
		workbook = new XSSFWorkbook();
	}

	private void writeHeaderRow() {
		sheet = workbook.createSheet("Users");
		Row row = sheet.createRow(0);

		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);

		row.createCell(0).setCellValue("ID");
		row.createCell(1).setCellValue("Họ tên");
		row.createCell(2).setCellValue("Email");
		row.createCell(3).setCellValue("SĐT");
		row.createCell(4).setCellValue("Vai trò");
		row.createCell(5).setCellValue("Trạng thái");

		for (int i = 0; i <= 5; i++) {
			row.getCell(i).setCellStyle(style);
		}
	}

	private void writeDataRows(List<User> users) {
		int rowCount = 1;

		for (User user : users) {
			Row row = sheet.createRow(rowCount++);

			row.createCell(0).setCellValue(user.getId());
			row.createCell(1).setCellValue(user.getFullName());
			row.createCell(2).setCellValue(user.getEmail());
			row.createCell(3).setCellValue(user.getPhoneNumber());
			row.createCell(4).setCellValue(user.getRole());
			row.createCell(5).setCellValue(user.getStatus());
		}

		for (int i = 0; i <= 5; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	public void export(List<User> users, HttpServletResponse response) throws IOException {
		writeHeaderRow();
		writeDataRows(users);

		ServletOutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		workbook.close();
		outputStream.close();
	}
}
