import { Pipe, PipeTransform, inject } from '@angular/core';
import { CurrencyService } from '../services/currency.service';

/**
 * Usage in templates:
 *   {{ ticket.loanAmount | currencyConvert }}
 *   {{ ticket.loanAmount | currencyConvert:0 }}   (no decimals)
 */
@Pipe({
  name:       'currencyConvert',
  standalone: true,
  pure:       false  // impure so it updates when signal changes
})
export class CurrencyConvertPipe implements PipeTransform {
  private currencyService = inject(CurrencyService);

  transform(lkrAmount: number | null | undefined, decimals = 2): string {
    if (lkrAmount == null) return '—';
    return this.currencyService.format(lkrAmount, decimals);
  }
}