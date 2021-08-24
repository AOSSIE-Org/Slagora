import { Component } from '@angular/core';

@Component({
  selector: 'ngx-footer',
  styleUrls: ['./footer.component.scss'],
  template: `
    <span class="created-by">Created with ♥ by <b><a href="https://hiis.io" target="_blank">Hiis.io</a></b> 2021</span>
  `,
})
export class FooterComponent {
}
