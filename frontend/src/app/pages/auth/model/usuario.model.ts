export interface Usuario {
  nome: string;
  email: string;
}

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface CadastroRequest extends LoginRequest {
  nome: string;
}
