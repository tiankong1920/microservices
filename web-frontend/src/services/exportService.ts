import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

export interface ExportField {
  key: string;
  label: string;
  format?: (value: any) => string;
}

export interface ExportOptions {
  filename?: string;
  fields?: ExportField[];
  dateFormat?: string;
}

class ExportService {
  static exportToCSV<T>(data: T[], options: ExportOptions = {}): void {
    const { filename = 'export', fields } = options;
    const headers = fields ? fields.map(f => f.label) : this.extractHeaders(data[0]);
    const rows = data.map(item => this.extractRow(item, fields));
    
    const csvContent = [
      headers.join(','),
      ...rows.map(row => row.join(','))
    ].join('\n');
    
    const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `${filename}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  static exportToExcel<T>(data: T[], options: ExportOptions = {}): void {
    const { filename = 'export', fields } = options;
    const headers = fields ? fields.map(f => f.label) : this.extractHeaders(data[0]);
    const rows = data.map(item => this.extractRow(item, fields));
    
    let excelContent = headers.join('\t') + '\n';
    rows.forEach(row => {
      excelContent += row.join('\t') + '\n';
    });
    
    const blob = new Blob(['\uFEFF' + excelContent], { type: 'application/vnd.ms-excel;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `${filename}.xls`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  static exportToJSON<T>(data: T[], options: ExportOptions = {}): void {
    const { filename = 'export' } = options;
    const jsonContent = JSON.stringify(data, null, 2);
    const blob = new Blob([jsonContent], { type: 'application/json;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `${filename}.json`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  private static extractHeaders<T>(item: T): string[] {
    if (!item) return [];
    return Object.keys(item).map(key => key);
  }

  private static extractRow<T>(item: T, fields?: ExportField[]): string[] {
    if (!item) return [];
    
    if (fields) {
      return fields.map(field => {
        const value = (item as any)[field.key];
        if (field.format) {
          return field.format(value);
        }
        return this.formatValue(value);
      });
    }
    
    return Object.values(item).map(value => this.formatValue(value));
  }

  private static formatValue(value: any): string {
    if (value === null || value === undefined) return '';
    if (typeof value === 'object') return JSON.stringify(value);
    return String(value);
  }

  static exportToPDF<T>(data: T[], options: ExportOptions = {}): void {
    const { filename = 'export', fields } = options;
    const headers = fields ? fields.map(f => f.label) : this.extractHeaders(data[0]);
    const rows = data.map(item => this.extractRow(item, fields));
    
    const doc = new jsPDF('landscape', 'mm', 'a4');
    const pageWidth = doc.internal.pageSize.getWidth();
    
    // Title
    doc.setFontSize(16);
    doc.text(filename.charAt(0).toUpperCase() + filename.slice(1), pageWidth / 2, 15, { align: 'center' });
    
    // Subtitle with date
    doc.setFontSize(10);
    const now = new Date();
    const dateStr = now.toLocaleDateString('zh-CN') + ' ' + now.toLocaleTimeString('zh-CN');
    doc.text('导出时间: ' + dateStr, pageWidth / 2, 22, { align: 'center' });
    
    // Data table
    autoTable(doc, {
      head: [headers],
      body: rows,
      startY: 28,
      styles: {
        fontSize: 8,
        cellPadding: 2,
        overflow: 'linebreak',
      },
      headStyles: {
        fillColor: [66, 66, 66],
        textColor: 255,
        fontStyle: 'bold',
        halign: 'center',
      },
      alternateRowStyles: {
        fillColor: [245, 245, 245],
      },
      margin: { top: 28 },
      didDrawPage: () => {
        // Page number
        const pageCount = doc.getNumberOfPages();
        const pageNum = doc.getCurrentPageInfo().pageNumber;
        doc.setFontSize(8);
        doc.text(
          `第 ${pageNum} 页 / 共 ${pageCount} 页`,
          pageWidth / 2,
          doc.internal.pageSize.getHeight() - 10,
          { align: 'center' }
        );
      },
    });
    
    doc.save(`${filename}.pdf`);
  }

  static async exportData<T>(data: T[], fields: ExportField[], filename: string, format: string = 'csv'): Promise<void> {
    const options: ExportOptions = { filename, fields };
    
    switch (format.toLowerCase()) {
      case 'csv':
        this.exportToCSV(data, options);
        break;
      case 'excel':
      case 'xls':
        this.exportToExcel(data, options);
        break;
      case 'json':
        this.exportToJSON(data, options);
        break;
      case 'pdf':
        this.exportToPDF(data, options);
        break;
      default:
        this.exportToCSV(data, options);
    }
  }
}

export default ExportService;