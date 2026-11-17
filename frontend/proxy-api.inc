proxy_pass http://api:8080;
proxy_set_header Host $host;
client_max_body_size 64k;
