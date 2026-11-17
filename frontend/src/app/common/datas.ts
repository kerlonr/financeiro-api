/**
 * A API trabalha com datas sem horário no formato "AAAA-MM-DD".
 * As conversões usam a data local (não UTC) para o dia nunca "voltar" por causa do fuso.
 */
export function paraDataIso(data: Date): string {
  const mes = String(data.getMonth() + 1).padStart(2, '0');
  const dia = String(data.getDate()).padStart(2, '0');
  return `${data.getFullYear()}-${mes}-${dia}`;
}

export function deDataIso(dataIso: string): Date {
  const [ano, mes, dia] = dataIso.split('-').map(Number);
  return new Date(ano, mes - 1, dia);
}
