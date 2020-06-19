package com.mesquita.transcolarapp.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.iid.InstanceIdResult;
import com.mesquita.transcolarapp.R;
import com.mesquita.transcolarapp.config.ConfiguracaoFirebase;
import com.mesquita.transcolarapp.config.Util;
import com.mesquita.transcolarapp.model.Motorista;
import com.mesquita.transcolarapp.model.Usuario;

public class CadastroActivity extends AppCompatActivity {
    private EditText campoNome;
    private EditText campoEmail;
    private EditText campoSenha;
    private RadioGroup campoTpUsuario;
    private FirebaseAuth autenticacao;

    Usuario usuario = new Usuario();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        campoNome = findViewById(R.id.editNome);
        campoEmail = findViewById(R.id.editEmail);
        campoSenha = findViewById(R.id.editSenha);
        campoTpUsuario = findViewById(R.id.rdGroupUsr);

        recuperarToken();
    }

    public void cadastrar(View view){
        String textoNome = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();
        int tipoUsuarioSelecionado = campoTpUsuario.getCheckedRadioButtonId();

        //Validar se os campos foram preenchidos
        if (!textoNome.isEmpty()){
            if (Util.isEmailValido(textoEmail)){
                if (!Util.isCampoVazio(textoSenha)){
                    if (tipoUsuarioSelecionado != -1) {
                        usuario.setNome(textoNome);
                        usuario.setEmail(textoEmail);
                        usuario.setSenha(textoSenha);
                        if (tipoUsuarioSelecionado == findViewById(R.id.rbMotorista).getId()) {
                            usuario.setTipoUsuario("motorista");
                        } else if (tipoUsuarioSelecionado == findViewById(R.id.rbAluno).getId()) {
                            usuario.setTipoUsuario("aluno");
                        } else {
                            usuario.setTipoUsuario("responsavel");
                        }
                        cadastrarUsuario(usuario);
                    } else {
                        Toast.makeText(CadastroActivity.this, "Selecione o tipo de usuário", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CadastroActivity.this, "Preencha a Senha", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CadastroActivity.this, "Preencha o Email", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(CadastroActivity.this, "Preencha o Nome", Toast.LENGTH_SHORT).show();
        }
    }

    public void cadastrarUsuario(final Usuario u){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(u.getEmail(), u.getSenha())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                try {
                                    //Após cadastrar o usuário na área de autenticação,
                                    //grava o restante dos dados no banco de dados
                                    u.setId(autenticacao.getUid());
                                    u.salvar();

                                    //Cadastrar Motorista/Responsavel/Aluno
                                    if (u.getTipoUsuario().equals("motorista")) {
                                        Motorista m = new Motorista();
                                        m.setId(u.getId());
                                        m.salvar();
                                    } else if (u.getTipoUsuario().equals("aluno")) {
                                        //TODO Implementar
                                    } else {
                                        //TODO Implementar
                                    }

                                    Toast.makeText(CadastroActivity.this,
                                            "Usuário cadastrado com sucesso!",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                } catch (Exception e){
                                    e.printStackTrace();
                                }
                            } else {
                                String excecao;
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthWeakPasswordException e){
                                    excecao = "Digite uma senha mais forte!";
                                } catch (FirebaseAuthInvalidCredentialsException e){
                                    excecao = "Digite um email valido!";
                                } catch (FirebaseAuthUserCollisionException e){
                                    excecao = "Esta conta já foi cadastrada!";
                                } catch (Exception e) {
                                    excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                                    e.printStackTrace();
                                }
                                Toast.makeText(CadastroActivity.this,
                                        excecao,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
    }

    private void recuperarToken(){
        ConfiguracaoFirebase.getFirebaseInstanceId().getInstanceId()
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                      @Override
                      public void onSuccess(InstanceIdResult instanceIdResult) {
                          String token = instanceIdResult.getToken();
                          Log.i("recuperarToken", "toke:" + token);
                          usuario.setToken(token);
                      }
                  }
        );
    }
}
