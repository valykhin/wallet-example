select 'POST||/api/v1/wallets||good||{"amount": 1010,"wallet_id": "'||w.id||'","operation_type": "DEPOSIT"}' from wallets w
union
select 'POST||/api/v1/wallets||good||{"amount": 1000,"wallet_id": "'||w.id||'","operation_type": "WITHDRAW"}' from wallets w
union
select 'GET||/api/v1/wallets/'||w.id||'||good||' from wallets w