FROM python:3.14-slim

# ENV PYTHONDONTWRITEBYTECODE=1
# ENV PYTHONUNBUFFERED=1

WORKDIR /server

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

RUN chmod +x /server/run.sh

EXPOSE 8000

CMD ["sh", "/server/run.sh"]

# alembic revision --autogenerate -m "MESSAGE HERE"
# alembic upgrade head