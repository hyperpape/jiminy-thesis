from scipy.stats import binom

def chance_loop_errors(iterations, valid, total):
    single_var_valid = (1 / (total / valid))
    three_vars_valid = single_var_valid ** 5
    return 1 - ((1 - three_vars_valid) ** iterations)

for i in range(10):
    print(binom.cdf(k=i, n=10, p=chance_loop_errors(10000, (2 ** 16) - 1024, 1024 ** 2)))
