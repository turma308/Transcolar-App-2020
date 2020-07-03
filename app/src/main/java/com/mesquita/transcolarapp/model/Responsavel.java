package com.mesquita.transcolarapp.model;

import com.google.firebase.database.DatabaseReference;
import com.mesquita.transcolarapp.config.ConfiguracaoFirebase;

public class Responsavel extends Usuario {
    private String CEP;
    private String RG;

    public Responsavel() {
        super();
    }

    public String getCEP() {
        return CEP;
    }

    public void setCEP(String cnh) {
        this.CEP = CEP;
    }

    public String getRG() {
        return RG;
    }

    public void setRG(String RG) {
        this.RG = RG;
    }

    public void salvar(){
        //super.salvar();

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference responsavelRef = firebaseRef.child("responsaveis").child(getId());

        responsavelRef.setValue(this);
    }


}
