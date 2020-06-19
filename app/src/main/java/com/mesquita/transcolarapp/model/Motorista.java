package com.mesquita.transcolarapp.model;

import com.google.firebase.database.DatabaseReference;
import com.mesquita.transcolarapp.config.ConfiguracaoFirebase;

public class Motorista extends Usuario {
    private String cnh = "1234567890";

    public Motorista() {
        super();
    }

    public String getCnh() {
        return cnh;
    }

    public void setCnh(String cnh) {
        this.cnh = cnh;
    }

    public void salvar(){
        //super.salvar();

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference motoristaRef = firebaseRef.child("motoristas").child(getId());

        motoristaRef.setValue(this);
    }

}
