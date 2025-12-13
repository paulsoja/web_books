create table email_otp (
                           id bigserial primary key,
                           email text not null,
                           purpose text not null,
                           code_hash text not null,
                           created_at timestamp not null,
                           expires_at timestamp not null,
                           consumed_at timestamp null,
                           attempts int not null default 0,
                           request_ip text null,
                           user_agent text null
);

create index email_otp_email_idx on email_otp (email);
create index email_otp_active_idx on email_otp (email, purpose, expires_at) where consumed_at is null;