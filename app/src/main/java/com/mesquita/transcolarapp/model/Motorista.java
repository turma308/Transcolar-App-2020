package com.mesquita.transcolarapp.model;

import com.google.firebase.database.DatabaseReference;
import com.mesquita.transcolarapp.config.ConfiguracaoFirebase;

import java.util.ArrayList;
import java.util.List;

public class Motorista extends Usuario {
    private String cnh = "1234567890";
    private List<Responsavel> clientes = new ArrayList<>();

    public Motorista() {
        super();
    }

    public String getCnh() {
        return cnh;
    }

    public void setCnh(String cnh) {
        this.cnh = cnh;
    }

    public List<Responsavel> getClientes() {
        return clientes;
    }

    public void setClientes(List<Responsavel> clientes) {
        this.clientes = clientes;
    }

    public void salvar(){
        //super.salvar();

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference motoristaRef = firebaseRef.child("motoristas").child(getId());

        motoristaRef.setValue(this);
    }

}
