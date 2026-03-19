FROM python:3.14-slim

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

WORKDIR /server

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 8000

CMD alembic upgrade head && uvicorn src:app --host 0.0.0.0 --port 8000 --reload


# alembic revision --autogenerate -m "MESSAGE HERE"
# alembic upgrade head