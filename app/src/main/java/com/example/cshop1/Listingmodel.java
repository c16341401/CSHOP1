package com.example.cshop1;

public class Listingmodel {

    private String ptid, ptitle, pdescr, plikes, pimage, pTime, uid, udp, uname, uemail, pcategory, pprice;

    public Listingmodel() {
    }

    public String getPtid() {
        return ptid;
    }

    public void setPtid(String ptid) {
        this.ptid = ptid;
    }

    public String getPtitle() {
        return ptitle;
    }

    public void setPtitle(String ptitle) {
        this.ptitle = ptitle;
    }

    public String getPdescr() {
        return pdescr;
    }

    public void setPdescr(String pdescr) {
        this.pdescr = pdescr;
    }

    public String getPlikes() {
        return plikes;
    }

    public void setPlikes(String plikes) {
        this.plikes = plikes;
    }

    public String getPimage() {
        return pimage;
    }

    public void setPimage(String pimage) {
        this.pimage = pimage;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUdp() {
        return udp;
    }

    public void setUdp(String udp) {
        this.udp = udp;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getUemail() {
        return uemail;
    }

    public void setUemail(String uemail) {
        this.uemail = uemail;
    }

    public String getPcategory() {
        return pcategory;
    }

    public void setPcategory(String pcategory) {
        this.pcategory = pcategory;
    }

    public String getPprice() {
        return pprice;
    }

    public void setPprice(String pprice) {
        this.pprice = pprice;
    }

    public Listingmodel(String ptid, String ptitle, String pdescr, String plikes, String pimage, String pTime, String uid, String udp, String uname, String uemail, String pcategory, String pprice) {
        this.ptid = ptid;
        this.ptitle = ptitle;
        this.pdescr = pdescr;
        this.plikes = plikes;
        this.pimage = pimage;
        this.pTime = pTime;
        this.uid = uid;
        this.udp = udp;
        this.uname = uname;
        this.uemail = uemail;
        this.pcategory = pcategory;
        this.pprice = pprice;
    }
}
