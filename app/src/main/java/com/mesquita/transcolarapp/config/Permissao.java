package com.mesquita.transcolarapp.config;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissao {

    public static boolean validarPermissoes(String[] permissoes, Activity activity, int requestCode){

        //Teste a versão do SDK pois só é necessário
        //validar permissões para versões iguais ou
        //superiores que a Marshmallow(23)
        if (Build.VERSION.SDK_INT >= 23){
            List<String> listaPermissoes = new ArrayList<>();

            //Percorre a lista de permissões recebida,
            //verificando uma a uma se já possui a liberação
            for (String perm: permissoes) {
                if (!(ContextCompat.checkSelfPermission(activity, perm) == PackageManager.PERMISSION_GRANTED)){
                    listaPermissoes.add(perm);
                }
            }

            //Caso a lista de permissões esteja vazia não é necessário solicitar
            //novas permissões
            if (listaPermissoes.isEmpty()){
                return true;
            }

            //Converte List em Array
            String[] novasPermissões = new String[listaPermissoes.size()];
            listaPermissoes.toArray(novasPermissões);

            //Solicita permissões
            ActivityCompat.requestPermissions(activity, novasPermissões, requestCode);

        }

        return true;
    }
}
