import { Component, ElementRef, ViewChild, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ChatbotService } from '../../../core/services/chatbot.service';
import { ChatTurn, SuggestedLink } from '../../../core/models/chatbot.model';

interface DisplayMessage extends ChatTurn {
  links?: SuggestedLink[] | null;
}

@Component({
  selector: 'app-chat-widget',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TranslatePipe],
  templateUrl: './chat-widget.component.html',
  styleUrl: './chat-widget.component.scss'
})
export class ChatWidgetComponent {
  @ViewChild('scrollAnchor') scrollAnchor?: ElementRef<HTMLDivElement>;

  open      = signal(false);
  sending   = signal(false);
  draft     = '';
  messages  = signal<DisplayMessage[]>([]);

  constructor(
    private chatbotService: ChatbotService,
    private translate: TranslateService
  ) {}

  toggle(): void {
    this.open.set(!this.open());
    if (this.open() && this.messages().length === 0) {
      this.messages.set([{
        role: 'assistant',
        content: this.translate.instant('chatWidget.greeting')
      }]);
    }
  }

  send(): void {
    const text = this.draft.trim();
    if (!text || this.sending()) return;

    const history: ChatTurn[] = this.messages().map(m => ({ role: m.role, content: m.content }));

    this.messages.update(msgs => [...msgs, { role: 'user', content: text }]);
    this.draft = '';
    this.sending.set(true);
    this.scrollToBottom();

    this.chatbotService.sendMessage(text, history).subscribe({
      next: (res) => {
        this.messages.update(msgs => [...msgs, { role: 'assistant', content: res.reply, links: res.links }]);
        this.sending.set(false);
        this.scrollToBottom();
      },
      error: () => {
        this.messages.update(msgs => [...msgs, {
          role: 'assistant',
          content: this.translate.instant('chatWidget.errorGeneric')
        }]);
        this.sending.set(false);
        this.scrollToBottom();
      }
    });
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      this.scrollAnchor?.nativeElement.scrollIntoView({ behavior: 'smooth' });
    }, 50);
  }
}