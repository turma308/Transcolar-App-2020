package com.mesquita.transcolarapp.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mesquita.transcolarapp.R;
import com.mesquita.transcolarapp.config.ConfiguracaoFirebase;
import com.mesquita.transcolarapp.config.Permissao;
import com.mesquita.transcolarapp.config.Util;
import com.mesquita.transcolarapp.model.Aluno;
import com.mesquita.transcolarapp.model.Responsavel;
import com.mesquita.transcolarapp.model.Usuario;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.core.app.ActivityCompat.startActivityForResult;

public class ConfiguracoesAlunoActivity extends AppCompatActivity {
    private EditText    nome;
    private EditText    endereco;
    private EditText    CPF;
    private EditText    telefone;
    private RadioGroup  campoSexo;
    private EditText  nomeResponsavel;
    private EditText  cpfResponsavel;
    private CircleImageView fotoPerfil;


    private StorageReference storageReference;

    private Usuario usr;
    private Aluno aln;

    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;

    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes_alunos);

        getSupportActionBar().setTitle("Configurações - Aluno");

        storageReference = ConfiguracaoFirebase.getFirebaseStorage();

        //Validar permissões
        Permissao.validarPermissoes(permissoes, this, 1);

        nome       = findViewById(R.id.etNome);
        endereco   = findViewById(R.id.etEndereco);
        CPF        = findViewById(R.id.etCPF);
        telefone   = findViewById(R.id.etTelefone);
        campoSexo  = findViewById(R.id.rdGroupSexo);
        fotoPerfil = findViewById(R.id.civFotoPerfil);
//        nomeResponsavel = findViewById(R.id.etNomeResponsavel);
//        cpfResponsavel = findViewById(R.id.etCpfResponsavel);

        //GET EXTRA...
        usr = (Usuario) getIntent().getSerializableExtra("usr");
        if (usr != null){
            nome.setText(usr.getNome());
            endereco.setText(usr.getEndereco());
            CPF.setText(usr.getCpf());
            telefone.setText(usr.getTelefone());

//            cpfResponsavel.setText(aln.getCpfResponsavel());
//            nomeResponsavel.setText(aln.getNomeResponsavel());

            //TODO Aqui corrigir atribuição do sexo.

            //Instancia um Responsável
            aln = buscaAluno(usr.getId());

            //Recupera a foto do usuário
            StorageReference imagemRef = storageReference
                    .child("imagens")
                    .child(usr.getId() + ".jpeg");
            final long ONE_MEGABYTE = 1024 * 1024;
            imagemRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    fotoPerfil.setImageBitmap(Bitmap.createScaledBitmap(bmp, fotoPerfil.getWidth(),
                            fotoPerfil.getHeight(), false));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    fotoPerfil.setImageResource(R.drawable.padrao);
                }
            });

        }
    }

    private Aluno buscaAluno(String id) {
        aln = new Aluno();
        aln.setId(usr.getId());
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        final DatabaseReference alunoRef = firebaseRef.child("alunos").child(id);

        alunoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                aln = dataSnapshot.getValue(Aluno.class);
                //TODO buscar os dados dos alunos

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return aln;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults){
            if (permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            Bitmap imagem = null;

            try {
                switch (requestCode){
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                    case SELECAO_GALERIA:
                        Uri uriImagemSelec = data.getData();
//                        ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uriImagemSelec);
//                       imagem = ImageDecoder.decodeBitmap(source);
                        break;
                }

                if (imagem != null){
                    fotoPerfil.setImageBitmap(imagem);

                    //Converter os dados da imagem para armazenar no Firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Salvar imagem no Firebase
                    StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child(usr.getId() + ".jpeg");
                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ConfiguracoesAlunoActivity.this,
                                    "Erro ao fazer upload da foto.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(ConfiguracoesAlunoActivity.this,
                                    "Foto armazenada com sucesso.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void alertaValidacaoPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões de acesso.");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void gravarAluno(View view) {
        String textoNome              = nome.getText().toString();
        String textoEndereco          = endereco.getText().toString();
        String textoCPF               = CPF.getText().toString();
        String textoTelefone          = telefone.getText().toString();
        int itemRadioGroupSelecionado = campoSexo.getCheckedRadioButtonId();
//        String textoNomeResponsavel = nomeResponsavel.getText().toString();
//        String textoCpfResponsavel =    cpfResponsavel.getText().toString();


        //Validar se os campos foram preenchidos
        if (!isCampoVazio(textoNome)){

            if (!isCampoVazio(textoEndereco)){

                if(!isCampoVazio(textoCPF)){

                    if(!isCampoVazio(textoTelefone)){

//                        if(!isCampoVazio(textoNomeResponsavel)){
//
//                            if(!isCampoVazio(textoCpfResponsavel)){


                        if (itemRadioGroupSelecionado != -1){

                            RadioButton rbSexoSelecioando = findViewById(itemRadioGroupSelecionado);
                            String textoSexo =  rbSexoSelecioando.getText().toString();
                            usr.setNome(textoNome);
                            usr.setEndereco(textoEndereco);
                            usr.setCpf(textoCPF);
                            usr.setTelefone(textoTelefone);
                            usr.setSexo(textoSexo);
//                            aln.setCpfResponsavel(textoCpfResponsavel);
//                            aln.setNomeResponsavel(textoNomeResponsavel);



                            usr.salvar();
                            aln.salvar();

                            Util.esconderTeclado(view);

                            Toast.makeText(ConfiguracoesAlunoActivity.this,
                                    "Dados salvos com sucesso!",
                                    Toast.LENGTH_SHORT).show();

                        }else{
                            Toast.makeText(ConfiguracoesAlunoActivity.this, "Selecione o sexo", Toast.LENGTH_SHORT).show();
                        }
//                    }else{
//                        Toast.makeText(ConfiguracoesAlunoActivity.this, "Preencha o cpf responsavel", Toast.LENGTH_SHORT).show();
//                        cpfResponsavel.requestFocus();
//                    }
//                        }else{
//                            Toast.makeText(ConfiguracoesAlunoActivity.this, "Selecione o nome responsável", Toast.LENGTH_SHORT).show();
//                            nomeResponsavel.requestFocus();
//                        }
                    }else{
                        Toast.makeText(ConfiguracoesAlunoActivity.this, "Preencha o telefone", Toast.LENGTH_SHORT).show();
                        telefone.requestFocus();
                    }
                }else{
                    Toast.makeText(ConfiguracoesAlunoActivity.this, "Preencha o CPF", Toast.LENGTH_SHORT).show();
                    CPF.requestFocus();
                }

            } else {
                Toast.makeText(ConfiguracoesAlunoActivity.this, "Preencha o endereço", Toast.LENGTH_SHORT).show();
                endereco.requestFocus();
            }

        } else {
            Toast.makeText(ConfiguracoesAlunoActivity.this, "Preencha o Nome", Toast.LENGTH_SHORT).show();
            nome.requestFocus();
        }
    }

    private boolean isCampoVazio(String valor){

        boolean resultado = (TextUtils.isEmpty(valor) || valor.trim().isEmpty() );
        return resultado;

    }

    public void abreCamera(View view) {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(i, SELECAO_CAMERA);
        }
    }

    public void abreGaleria(View view){
        Intent i = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (i.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(i, SELECAO_GALERIA);
        }

    }



}


