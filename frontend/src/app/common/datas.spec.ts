import { deDataIso, paraDataIso } from './datas';

describe('datas', () => {
  it('converte a data local sem mudar o dia por causa do fuso', () => {
    // 23h: em UTC já seria o dia seguinte. toISOString() erraria aqui.
    expect(paraDataIso(new Date(2026, 10, 30, 23, 30))).toBe('2026-11-30');
  });

  it('completa mês e dia com zero à esquerda', () => {
    expect(paraDataIso(new Date(2026, 0, 5))).toBe('2026-01-05');
  });

  it('lê "AAAA-MM-DD" como meia-noite local do mesmo dia', () => {
    const data = deDataIso('2026-03-01');
    expect([data.getFullYear(), data.getMonth(), data.getDate(), data.getHours()]).toEqual([
      2026, 2, 1, 0,
    ]);
  });

  it('ida e volta preservam a data', () => {
    expect(paraDataIso(deDataIso('2026-12-31'))).toBe('2026-12-31');
  });
});
