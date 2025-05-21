insert into wallets (
    id, balance
)
select
    gen_random_uuid(),
    ceil(random() * 100000)
from generate_series(1, 100) s(i)