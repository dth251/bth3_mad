package com.example.quanlynhansu;

import java.text.NumberFormat;
import java.util.Locale;

/** Model chứa đúng 9 thuộc tính tương ứng với bảng NhanSu. */
public class NhanSu {
    private String maNhanSu;
    private String hoTen;
    private int namSinh;
    private String gioiTinh;
    private String queQuan;
    private String trinhDo;
    private String anh;
    private double mucLuong;
    private String phongBan;

    public NhanSu(String maNhanSu, String hoTen, int namSinh, String gioiTinh,
                  String queQuan, String trinhDo, String anh, double mucLuong, String phongBan) {
        this.maNhanSu = maNhanSu;
        this.hoTen = hoTen;
        this.namSinh = namSinh;
        this.gioiTinh = gioiTinh;
        this.queQuan = queQuan;
        this.trinhDo = trinhDo;
        this.anh = anh;
        this.mucLuong = mucLuong;
        this.phongBan = phongBan;
    }

    public String getMaNhanSu() { return maNhanSu; }
    public void setMaNhanSu(String maNhanSu) { this.maNhanSu = maNhanSu; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public int getNamSinh() { return namSinh; }
    public void setNamSinh(int namSinh) { this.namSinh = namSinh; }
    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }
    public String getQueQuan() { return queQuan; }
    public void setQueQuan(String queQuan) { this.queQuan = queQuan; }
    public String getTrinhDo() { return trinhDo; }
    public void setTrinhDo(String trinhDo) { this.trinhDo = trinhDo; }
    public String getAnh() { return anh; }
    public void setAnh(String anh) { this.anh = anh; }
    public double getMucLuong() { return mucLuong; }
    public void setMucLuong(double mucLuong) { this.mucLuong = mucLuong; }
    public String getPhongBan() { return phongBan; }
    public void setPhongBan(String phongBan) { this.phongBan = phongBan; }

    public String getLuongHienThi() {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        format.setMaximumFractionDigits(2);
        return format.format(mucLuong) + " VNĐ";
    }
}
