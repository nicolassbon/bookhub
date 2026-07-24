import { Component, inject, signal, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { GoalsService } from '../../core/services/goals.service';
import { YearlyGoalResponse } from '../../core/api/contracts/goals';

@Component({
  selector: 'app-goals-widget',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './goals-widget.html',
  styleUrl: './goals-widget.scss'
})
export class GoalsWidgetComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly goalsService = inject(GoalsService);

  readonly currentYear = new Date().getFullYear();
  readonly goal = signal<YearlyGoalResponse | null>(null);
  readonly isEditing = signal(false);
  readonly isSaving = signal(false);

  readonly goalForm = this.fb.group({
    targetBooks: [12, [Validators.required, Validators.min(1), Validators.max(300)]]
  });

  ngOnInit() {
    this.loadGoal();
  }

  loadGoal() {
    this.goalsService.getYearlyGoal().subscribe({
      next: g => this.goal.set(g),
      error: () => this.goal.set(null)
    });
  }

  toggleEdit() {
    this.isEditing.update(v => !v);
  }

  onSaveGoal() {
    if (this.goalForm.invalid) return;

    this.isSaving.set(true);
    const target = this.goalForm.get('targetBooks')?.value!;

    this.goalsService.setYearlyGoal(target).subscribe({
      next: updated => {
        this.goal.set(updated);
        this.isSaving.set(false);
        this.isEditing.set(false);
      },
      error: () => this.isSaving.set(false)
    });
  }
}
