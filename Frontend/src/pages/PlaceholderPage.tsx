export function PlaceholderPage({
  icon,
  title,
  description,
}: {
  icon: string;
  title: string;
  description: string;
}) {
  return (
    <div className="page">
      <section className="container empty-state">
        <span className="material-symbols-outlined">{icon}</span>
        <h1>{title}</h1>
        <p>{description}</p>
        <span className="pill">Đã sẵn sàng để triển khai ở sprint tiếp theo</span>
      </section>
    </div>
  );
}
