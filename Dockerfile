FROM nginx:alpine

# Maybe be more specific about what files you need
COPY out.yaml resource/limits.json /usr/share/nginx/html/

# Create a basic nginx.conf if you don't have one
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80