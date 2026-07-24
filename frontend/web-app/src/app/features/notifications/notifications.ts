import { Component, inject, signal, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { NotificationsService } from '../../core/services/notifications.service';
import { NotificationResponse } from '../../core/api/contracts/notifications';
import { LoadingStateComponent } from '../shared/components/loading';
import { EmptyStateComponent } from '../shared/components/empty';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [DatePipe, LoadingStateComponent, EmptyStateComponent],
  templateUrl: './notifications.html',
  styleUrl: './notifications.scss'
})
export class NotificationsComponent implements OnInit {
  private readonly notificationsService = inject(NotificationsService);

  readonly notifications = signal<NotificationResponse[]>([]);
  readonly isLoading = signal(true);

  ngOnInit() {
    this.loadNotifications();
  }

  loadNotifications() {
    this.notificationsService.getNotifications().subscribe({
      next: list => {
        this.notifications.set(list);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  markAsRead(id: string) {
    this.notificationsService.markAsRead(id).subscribe({
      next: updated => {
        this.notifications.update(list => list.map(n => n.id === id ? updated : n));
      }
    });
  }
}
