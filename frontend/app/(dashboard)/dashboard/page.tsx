export default function DashboardPage() {
  return (
    <div>
      <h1 className="text-2xl font-bold text-slate-900 mb-6">대시보드</h1>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {[
          { label: '진행 중인 오더', value: '—', color: 'bg-blue-50 text-blue-700' },
          { label: '납기 임박 (3일)', value: '—', color: 'bg-red-50 text-red-700' },
          { label: '대기 중인 연차', value: '—', color: 'bg-yellow-50 text-yellow-700' },
          { label: '미정산 오더', value: '—', color: 'bg-green-50 text-green-700' },
        ].map(({ label, value, color }) => (
          <div key={label} className="bg-white rounded-xl border border-slate-200 p-5">
            <p className="text-sm text-slate-500 mb-1">{label}</p>
            <p className={`text-3xl font-bold ${color} inline-block px-2 rounded`}>{value}</p>
          </div>
        ))}
      </div>

      <div className="bg-white rounded-xl border border-slate-200 p-5">
        <h2 className="text-base font-semibold text-slate-700 mb-4">최근 오더</h2>
        <p className="text-sm text-slate-400">아직 등록된 오더가 없습니다.</p>
      </div>
    </div>
  );
}
