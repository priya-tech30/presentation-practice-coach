import os
from dotenv import load_dotenv
from openai import OpenAI

load_dotenv()
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

audio_file = open("test_audio.m4a", "rb")
transcript = client.audio.transcriptions.create(
    model="whisper-1",
    file=audio_file
)
print(transcript.text)