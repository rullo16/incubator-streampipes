# Use a Python base image
FROM python:3.9-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Python script file to the container
COPY bridge.py .

# Install dependencies if required
# RUN pip install ...

# Expose the port on which the server will run
EXPOSE 8000

# Start the server
CMD ["python3", "bridge.py"]